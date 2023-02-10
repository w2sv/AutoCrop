@file:Suppress("DEPRECATION")

package com.w2sv.screenshotlistening

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.database.CursorIndexOutOfBoundsException
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.common.collect.EvictingQueue
import com.w2sv.common.PermissionHandler
import com.w2sv.cropbundle.Screenshot
import com.w2sv.cropbundle.cropping.cropEdges
import com.w2sv.cropbundle.cropping.cropped
import com.w2sv.cropbundle.io.CROP_FILE_ADDENDUM
import com.w2sv.cropbundle.io.extensions.compressToAndCloseStream
import com.w2sv.cropbundle.io.extensions.loadBitmap
import com.w2sv.cropbundle.io.extensions.queryMediaStoreData
import com.w2sv.cropbundle.io.getDeleteRequestUri
import com.w2sv.cropbundle.io.utils.systemScreenshotsDirectory
import com.w2sv.kotlinutils.dateFromUnixTimestamp
import com.w2sv.kotlinutils.timeDelta
import com.w2sv.kotlinutils.tripleFromIterable
import com.w2sv.screenshotlistening.ScreenshotListener.OnCancelledFromNotificationListener.Companion.ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS
import com.w2sv.screenshotlistening.notifications.AppNotificationChannel
import com.w2sv.screenshotlistening.notifications.NotificationGroup
import com.w2sv.screenshotlistening.notifications.setChannelAndGetNotificationBuilder
import com.w2sv.screenshotlistening.services.abstrct.BoundService
import slimber.log.i
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.Date
import java.util.concurrent.TimeUnit

class ScreenshotListener : BoundService(),
                           PendingIntentAssociatedResourcesCleanupService.Client {

    companion object {
        fun startService(context: Context) {
            context.startService(
                getIntent(context)
            )
            i { "Starting ScreenshotListener" }
        }

        fun stopService(context: Context) {
            context.startService(
                getIntent(context)
                    .setAction(ACTION_STOP_SERVICE)
            )
            i { "Stopping ScreenshotListener" }
        }

        private const val ACTION_STOP_SERVICE = "com.w2sv.autocrop.STOP_SERVICE"

        private fun getIntent(context: Context): Intent =
            Intent(context, ScreenshotListener::class.java)

        fun startCleanupService(context: Context, intent: Intent) {
            context.startService(
                intent.setClass(context, CleanupService::class.java)
            )
        }

        const val EXTRA_ATTEMPT_SCREENSHOT_DELETION = "com.w2sv.autocrop.extra.DELETE_SCREENSHOT"
        const val EXTRA_DELETE_REQUEST_URI = "com.w2sv.autocrop.extra.DELETE_REQUEST_URI"
        const val EXTRA_SCREENSHOT_MEDIASTORE_DATA = "com.w2sv.autocrop.extra.SCREENSHOT_MEDIASTORE_DATA"
        const val EXTRA_TEMPORARY_CROP_FILE_PATH = "com.w2sv.autocrop.extra.CROP_FILE_PATH"

        fun permissionHandlers(componentActivity: ComponentActivity): List<PermissionHandler> =
            buildList {
                add(
                    PermissionHandler(
                        componentActivity,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            Manifest.permission.READ_MEDIA_IMAGES
                        else
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                        "Media file access required for listening to screen captures",
                        "Go to app settings and grant media file access for screen capture listening to work"
                    )
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    add(
                        PermissionHandler(
                            componentActivity,
                            Manifest.permission.POST_NOTIFICATIONS,
                            "If you don't allow for the posting of notifications AutoCrop can't inform you about croppable screenshots",
                            "Go to app settings and enable notification posting for screen capture listening to work"
                        )
                    )
            }
    }

    class CleanupService : PendingIntentAssociatedResourcesCleanupService<ScreenshotListener>(ScreenshotListener::class.java)

    /**
     * Triggered by ScreenshotListener notification "Stop"-action and responsible for calling [stopService] &
     * triggering BroadcastReceivers subscribed to [ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS]
     */
    class OnCancelledFromNotificationListener : BroadcastReceiver() {

        companion object {
            const val ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS =
                "com.w2sv.autocrop.action.NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS"

            private const val PENDING_INTENT_REQUEST_CODE = 1447

            fun getPendingIntent(context: Context): PendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    PENDING_INTENT_REQUEST_CODE,
                    Intent(context, OnCancelledFromNotificationListener::class.java),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            stopService(context!!)

            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(
                    Intent(ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS)
                )
        }
    }

    /**
     * Launching
     */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        emitOnStartCommandLog(intent, flags, startId)

        when (intent!!.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                /**
                 * Start foreground service with notification and register [screenshotObserver]
                 */

                startForeground(
                    AppNotificationChannel.STARTED_FOREGROUND_SERVICE.childIdSeed,
                    foregroundServiceNotificationBuilder()
                        .build()
                )
                    .also { i { "Started foreground service" } }

                contentResolver.registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    true,
                    screenshotObserver
                )
                    .also { i { "Registered imageContentObserver" } }
            }
        }

        return START_REDELIVER_INTENT
    }

    private fun foregroundServiceNotificationBuilder(): NotificationCompat.Builder =
        setChannelAndGetNotificationBuilder(
            AppNotificationChannel.STARTED_FOREGROUND_SERVICE,
            AppNotificationChannel.STARTED_FOREGROUND_SERVICE.title
        )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You will receive a notification when AutoCrop detects a new croppable screenshot")
            )
            .addAction(
                NotificationCompat.Action(
                    com.w2sv.common.R.drawable.ic_cancel_24,
                    "Stop",
                    OnCancelledFromNotificationListener.getPendingIntent(this)
                )
            )

    /**
     * Observing/croppability determination
     */

    private val screenshotObserver by lazy {
        ScreenshotObserver(contentResolver, ::onNewScreenshotUri)
    }

    /**
     * Catches [IllegalStateException] in case of [uri] not being accessible due to still pending
     * (meaning that the device screenshot manager (= uri owner) is still accessing/processing the file,
     * such that the uri is blocked for other processes).
     *
     * @return [Boolean] indicating whether [uri] was accessible, meaning that this function should not
     * be called for the same [uri] again.
     */
    private fun onNewScreenshotUri(uri: Uri): Boolean =
        try {
            val screenshotBitmap = contentResolver.loadBitmap(uri)!!
            screenshotBitmap.cropEdges()?.let { cropEdges ->
                val screenshotMediaStoreData = Screenshot.MediaStoreData.query(contentResolver, uri)
                val deleteRequestUri = getDeleteRequestUri(screenshotMediaStoreData.id)
                val cropBitmap = screenshotBitmap.cropped(cropEdges)
                val temporaryCropFilePath = saveCropToTempFile(cropBitmap, screenshotMediaStoreData.id)

                showCroppableScreenshotDetectedNotification(
                    cropBitmap,
                    deleteRequestUri
                ) { clazz, isSaveIntent, notificationId, actionRequestCodes, putCancelNotificationExtra ->
                    Intent(this, clazz)
                        .putExtra(EXTRA_TEMPORARY_CROP_FILE_PATH, temporaryCropFilePath)
                        .putCleanupExtras(
                            notificationId,
                            actionRequestCodes,
                            putCancelNotificationExtra
                        )
                        .apply {
                            if (isSaveIntent) {
                                data = uri
                                putExtra(EXTRA_SCREENSHOT_MEDIASTORE_DATA, screenshotMediaStoreData)
                            }
                        }
                }
            }
            true
        }
        catch (ex: Exception) {
            when (ex) {
                is IllegalStateException, is NullPointerException, is FileNotFoundException -> Unit
                else -> throw ex
            }
            false
        }

    private fun saveCropToTempFile(crop: Bitmap, screenshotMediaStoreId: Long): String {
        val file = File.createTempFile(
            screenshotMediaStoreId.toString(),
            null
        )
        crop.compressToAndCloseStream(
            FileOutputStream(file),
            Bitmap.CompressFormat.PNG
        )
        return file.path
    }

    /**
     * Notifying
     */

    private fun showCroppableScreenshotDetectedNotification(
        cropBitmap: Bitmap,
        deleteRequestUri: Uri?,
        getActionIntentTemplate: (Class<*>, Boolean, Int, ArrayList<Int>, Boolean) -> Intent
    ) {
        val notificationId = notificationGroup.childrenIds.getNewId()
        val actionRequestCodes = notificationGroup.requestCodes.getAndAddMultipleNewIds(4)

        fun getActionIntent(clazz: Class<*>, isSaveIntent: Boolean, cancelNotificationExtra: Boolean): Intent =
            getActionIntentTemplate(
                clazz,
                isSaveIntent,
                notificationId,
                actionRequestCodes,
                cancelNotificationExtra
            )

        notificationGroup.addChild(notificationId) {
            setContentTitle("Crafted a new AutoCrop")
            addAction(
                NotificationCompat.Action(
                    null,
                    "Save",
                    PendingIntent.getService(
                        this@ScreenshotListener,
                        actionRequestCodes[0],
                        getActionIntent(CropIOService::class.java, isSaveIntent = true, cancelNotificationExtra = true),
                        REPLACE_CURRENT_PENDING_INTENT_FLAGS
                    )
                )
            )
            addAction(
                NotificationCompat.Action(
                    null,
                    "Save & delete Screenshot",
                    if (deleteRequestUri is Uri && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        PendingIntent.getActivity(
                            this@ScreenshotListener,
                            actionRequestCodes[1],
                            getActionIntent(
                                ScreenshotDeleteRequestActivity::class.java,
                                isSaveIntent = true,
                                cancelNotificationExtra = true
                            )
                                .putExtra(EXTRA_DELETE_REQUEST_URI, deleteRequestUri),
                            REPLACE_CURRENT_PENDING_INTENT_FLAGS
                        )
                    else
                        PendingIntent.getService(
                            this@ScreenshotListener,
                            actionRequestCodes[1],
                            getActionIntent(
                                CropIOService::class.java,
                                isSaveIntent = true,
                                cancelNotificationExtra = true
                            )
                                .putExtra(EXTRA_ATTEMPT_SCREENSHOT_DELETION, true),
                            REPLACE_CURRENT_PENDING_INTENT_FLAGS
                        )
                )
            )
            addAction(
                NotificationCompat.Action(
                    null,
                    "Dismiss",
                    PendingIntent.getService(
                        this@ScreenshotListener,
                        actionRequestCodes[2],
                        getActionIntent(
                            CleanupService::class.java,
                            isSaveIntent = false,
                            cancelNotificationExtra = true
                        ),
                        REPLACE_CURRENT_PENDING_INTENT_FLAGS
                    )
                )
            )
            setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(cropBitmap)
            )
            setDeleteIntent(
                PendingIntent.getService(
                    this@ScreenshotListener,
                    actionRequestCodes[3],
                    getActionIntent(
                        CleanupService::class.java,
                        isSaveIntent = false,
                        cancelNotificationExtra = false
                    ),
                    REPLACE_CURRENT_PENDING_INTENT_FLAGS
                )
            )
        }
    }

    override val notificationGroup = NotificationGroup(
        this,
        notificationChannel = AppNotificationChannel.DETECTED_NEW_CROPPABLE_SCREENSHOT,
        summaryBuilderConfigurator = { nChildren ->
            setContentTitle(getString(R.string.detected_n_croppable_screenshots, nChildren))
            setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText("Expand to select actions")
            )
        }
    )

    /**
     * Clean-up
     */

    override fun onCleanupFinishedListener(intent: Intent) {
        File(intent.getStringExtra(EXTRA_TEMPORARY_CROP_FILE_PATH)!!).delete()
    }

    override fun onDestroy() {
        super.onDestroy()

        contentResolver.unregisterContentObserver(screenshotObserver)
            .also { i { "Unregistered imageContentObserver" } }
    }
}

private const val REPLACE_CURRENT_PENDING_INTENT_FLAGS: Int =
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE

@Suppress("UnstableApiUsage")
private class ScreenshotObserver(
    private val contentResolver: ContentResolver,
    private val onNewScreenshotListener: (Uri) -> Boolean
) : ContentObserver(Handler(Looper.getMainLooper())) {

    companion object {
        /**
         * Number of URIs, that could possibly require simultaneous processing
         */
        const val MAX_URIS = 3
    }

    /**
     * Uris which have already been processed & are not to be considered again.
     */
    private val blacklist = EvictingQueue.create<Uri>(MAX_URIS)

    /**
     * Uris that have been asserted to correspond to a screenshot, but which were still pending
     * during last check.
     */
    private val pendingUris = EvictingQueue.create<Uri>(MAX_URIS)

    /**
     * According to own observation called on each change to a matching Uri, including
     * changes to their [MediaStore.MediaColumns].
     */
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        uri?.let {
            i { "Called onChange for $it" }
            if (!blacklist.contains(it)) {
                if (pendingUris.contains(it))
                    attemptOnNewScreenshotListenerInvocation(it, false)
                else {
                    when (it.isNewScreenshot()) {
                        true -> attemptOnNewScreenshotListenerInvocation(it, true)
                        false -> blacklist.add(it)
                        null -> pendingUris.add(it)
                    }
                }
            }
        }
    }

    private fun attemptOnNewScreenshotListenerInvocation(uri: Uri, addToPendingUrisIfInaccessible: Boolean) {
        if (onNewScreenshotListener(uri)) {
            blacklist.add(uri)
            i { "Added $uri to blacklist" }
        }
        else if (addToPendingUrisIfInaccessible)
            pendingUris.add(uri)
    }

    private fun Uri.isNewScreenshot(): Boolean? =
        try {
            val (absolutePath, fileName, dateAdded) = tripleFromIterable(
                contentResolver.queryMediaStoreData(
                    this,
                    arrayOf(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATA,  // e.g. /storage/emulated/0/Pictures/Screenshots/.pending-1665749333-Screenshot_20221007-140853687.png
                        MediaStore.Images.Media.DATE_ADDED
                    )
                )
            )

            !fileName.contains(CROP_FILE_ADDENDUM) &&  // exclude produced AutoCrop's
                    timeDelta(  // exclude files having already been on the file system and which were only triggered, due to a change of their metadata
                        dateFromUnixTimestamp(dateAdded),
                        Date(System.currentTimeMillis()),
                        TimeUnit.SECONDS
                    ) < 20 &&
                    (systemScreenshotsDirectory()?.let { absolutePath.contains(it.name) } == true ||  // infer whether or not actually a screenshot
                            fileName
                                .lowercase()
                                .contains("screenshot"))

        }
        catch (e: CursorIndexOutOfBoundsException) {
            null
        }
}