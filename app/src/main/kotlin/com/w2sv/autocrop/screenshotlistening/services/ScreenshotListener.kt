package com.w2sv.autocrop.screenshotlistening.services

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
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.common.collect.EvictingQueue
import com.w2sv.autocrop.R
import com.w2sv.autocrop.cropbundle.Screenshot
import com.w2sv.autocrop.cropbundle.cropping.cropEdges
import com.w2sv.autocrop.cropbundle.cropping.cropped
import com.w2sv.autocrop.cropbundle.io.CROP_FILE_ADDENDUM
import com.w2sv.autocrop.cropbundle.io.extensions.compressToAndCloseStream
import com.w2sv.autocrop.cropbundle.io.extensions.loadBitmap
import com.w2sv.autocrop.cropbundle.io.extensions.queryMediaStoreData
import com.w2sv.autocrop.cropbundle.io.getDeleteRequestUri
import com.w2sv.autocrop.cropbundle.io.utils.systemScreenshotsDirectory
import com.w2sv.autocrop.screenshotlistening.notifications.NotificationGroup
import com.w2sv.autocrop.screenshotlistening.notifications.NotificationId
import com.w2sv.autocrop.screenshotlistening.notifications.notificationBuilderWithSetChannel
import com.w2sv.autocrop.screenshotlistening.services.abstrct.BoundService
import com.w2sv.kotlinutils.dateFromUnixTimestamp
import com.w2sv.kotlinutils.timeDelta
import slimber.log.i
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.concurrent.TimeUnit

class ScreenshotListener :
    BoundService(),
    OnPendingIntentService.Client by OnPendingIntentService.Client.Impl(0) {

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

        private fun getIntent(context: Context): Intent =
            Intent(context, ScreenshotListener::class.java)

        private const val ACTION_STOP_SERVICE = "com.w2sv.autocrop.STOP_SERVICE"

        const val EXTRA_ATTEMPT_SCREENSHOT_DELETION = "com.w2sv.autocrop.DELETE_SCREENSHOT"
        const val EXTRA_DELETE_REQUEST_URI = "com.w2sv.autocrop.DELETE_REQUEST_URI"
        const val EXTRA_SCREENSHOT_MEDIASTORE_DATA = "com.w2sv.autocrop.SCREENSHOT_MEDIASTORE_DATA"
        const val EXTRA_TEMPORARY_CROP_FILE_PATH = "com.w2sv.autocrop.CROP_FILE_PATH"
    }

    /**
     * Triggered by ScreenshotListener "Stop"-action and responsible for calling [stopService] &
     * triggering BroadcastReceivers subscribed to [ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS]
     */
    class OnCancelledFromNotificationListener : BroadcastReceiver() {

        companion object {
            const val ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS =
                "com.w2sv.autocrop.NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS"

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

            LocalBroadcastManager
                .getInstance(context)
                .sendBroadcast(
                    Intent(ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS)
                )
        }
    }

    /**
     * Launching
     */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
                    NotificationId.STARTED_FOREGROUND_SERVICE.id,
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

        return super.onStartCommand(intent, flags, startId)
    }

    private fun foregroundServiceNotificationBuilder(): NotificationCompat.Builder =
        notificationBuilderWithSetChannel(
            NotificationId.STARTED_FOREGROUND_SERVICE.channelId,
            "Listening to screen captures"
        )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You will receive a notification when AutoCrop detects a new croppable screenshot")
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_cancel_24,
                    "Stop",
                    OnCancelledFromNotificationListener.getPendingIntent(this)
                )
            )

    /**
     * Observing/croppability determination
     */

    private val screenshotObserver = ScreenshotObserver(contentResolver, ::onNewScreenshotUri)

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
            val screenshotBitmap = contentResolver.loadBitmap(uri)
            screenshotBitmap.cropEdges()?.let { cropEdges ->
                val screenshotMediaStoreData = Screenshot.MediaStoreData.query(contentResolver, uri)
                val deleteRequestUri = getDeleteRequestUri(screenshotMediaStoreData.id)
                val cropBitmap = screenshotBitmap.cropped(cropEdges)
                val temporaryCropFilePath = saveCropToTempFile(cropBitmap, screenshotMediaStoreData.id)

                showCroppableScreenshotDetectedNotification(
                    uri,
                    screenshotMediaStoreData,
                    cropBitmap,
                    deleteRequestUri,
                    temporaryCropFilePath
                )
            }
            true
        }
        catch (ex: Exception) {
            when (ex) {
                is IllegalStateException, is NullPointerException -> Unit
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
        uri: Uri,
        screenshotMediaStoreData: Screenshot.MediaStoreData,
        cropBitmap: Bitmap,
        deleteRequestUri: Uri?,
        temporaryCropFilePath: String
    ) {
        val notificationId = notificationGroup.children.newId()
        val actionRequestCodes = requestCodes.makeAndAddMultiple(4)

        fun actionIntent(cls: Class<*>, saveIntent: Boolean, putCancelNotificationExtra: Boolean = true): Intent =
            Intent(this, cls)
                .putExtra(EXTRA_TEMPORARY_CROP_FILE_PATH, temporaryCropFilePath)
                .putOnPendingIntentServiceClientExtras(
                    notificationId,
                    actionRequestCodes,
                    putCancelNotificationExtra
                )
                .apply {
                    if (saveIntent) {
                        data = uri
                        putExtra(EXTRA_SCREENSHOT_MEDIASTORE_DATA, screenshotMediaStoreData)
                    }
                }

        notificationGroup.addChild(
            notificationId,
            notificationGroup.childBuilder("Fancy a crop?")
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Save",
                        PendingIntent.getService(
                            this,
                            actionRequestCodes[0],
                            actionIntent(CropIOService::class.java, true),
                            REPLACE_CURRENT_PENDING_INTENT_FLAGS
                        )
                    )
                )
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Save & delete",
                        if (deleteRequestUri is Uri && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                            PendingIntent.getActivity(
                                this,
                                actionRequestCodes[1],
                                actionIntent(DeleteRequestActivity::class.java, true)
                                    .putExtra(EXTRA_DELETE_REQUEST_URI, deleteRequestUri),
                                REPLACE_CURRENT_PENDING_INTENT_FLAGS
                            )
                        else
                            PendingIntent.getService(
                                this,
                                actionRequestCodes[1],
                                actionIntent(CropIOService::class.java, true)
                                    .putExtra(EXTRA_ATTEMPT_SCREENSHOT_DELETION, true),
                                REPLACE_CURRENT_PENDING_INTENT_FLAGS
                            )
                    )
                )
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Dismiss",
                        PendingIntent.getService(
                            this,
                            actionRequestCodes[2],
                            actionIntent(OnPendingIntentService::class.java, false),
                            REPLACE_CURRENT_PENDING_INTENT_FLAGS
                        )
                    )
                )
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(cropBitmap)
                )
                .setDeleteIntent(
                    PendingIntent.getService(
                        this,
                        actionRequestCodes[3],
                        actionIntent(
                            OnPendingIntentService::class.java,
                            saveIntent = false,
                            putCancelNotificationExtra = false
                        ),
                        REPLACE_CURRENT_PENDING_INTENT_FLAGS
                    )
                )
        )
    }

    override val notificationGroup = NotificationGroup(
        this,
        "Detected croppable screenshots",
        summaryId = NotificationId.DETECTED_NEW_CROPPABLE_SCREENSHOT,
        makeSummaryTitle = { "Detected $it croppable screenshots" },
        applyToSummaryBuilder = {
            setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText("Expand to save")
            )
        }
    )

    /**
     * Clean-up
     */

    override fun onPendingIntentService(intent: Intent) {
        File(intent.getStringExtra(EXTRA_TEMPORARY_CROP_FILE_PATH)!!).delete()
    }

    /**
     * Stop associated services and unregister [screenshotObserver]
     */
    override fun onDestroy() {
        super.onDestroy()

        stopService(Intent(this, OnPendingIntentService::class.java))
        stopService(Intent(this, CropIOService::class.java))

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
    /**
     * Uris which are not to be considered again.
     *
     * [EvictingQueue] maxSize determined with respect to the number of
     * screenshots, that could possibly be taken in the relatively short time interval, within which
     * a single Uri is processed.
     */
    private val recentBlacklist = EvictingQueue.create<Uri>(3)

    /**
     * Uris that have been asserted to correspond to a screenshot, but which were still pending
     * during last check.
     */
    private val pendingScreenshotUris = mutableSetOf<Uri>()

    /**
     * According to own observation called on each change to a matching Uri, including
     * changes to their [MediaStore.MediaColumns].
     */
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        uri?.let {
            i { "Called onChange for $it" }
            if (!recentBlacklist.contains(it)) {
                if (pendingScreenshotUris.contains(it) || it.isScreenshot() == true) {
                    if (onNewScreenshotListener(it)) {
                        recentBlacklist.add(it)
                        pendingScreenshotUris.remove(it)
                        i { "Added $uri to blacklist and removed it from pendingScreenshotUris" }
                    }
                    else
                        pendingScreenshotUris.add(it)
                            .also { i { "Added $uri to pendingScreenshotUris" } }
                }
                else
                    recentBlacklist.add(it)
                        .also { i { "Added $uri to blacklist" } }
            }
        }
    }

    /**
     * Checks if [this] not corresponding to AutoCrop-file and if its file path contains either the
     * [systemScreenshotsDirectory]name or the word 'screenshot'.
     */
    private fun Uri.isScreenshot(): Boolean? {
        try {
            val mediaStoreData = contentResolver.queryMediaStoreData(
                this,
                arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,  // e.g. /storage/emulated/0/Pictures/Screenshots/.pending-1665749333-Screenshot_20221007-140853687.png
                    MediaStore.Images.Media.DATE_ADDED
                )
            )

            val absolutePath = mediaStoreData[0]
            val fileName = mediaStoreData[1]
            val dateAdded = mediaStoreData[2]

            return !fileName.contains(CROP_FILE_ADDENDUM) &&
                    (systemScreenshotsDirectory()?.let {
                        absolutePath.contains(it.name)
                    } == true || fileName.lowercase().contains("screenshot")) &&
                    timeDelta(
                        dateFromUnixTimestamp(dateAdded),
                        Date(System.currentTimeMillis()),
                        TimeUnit.SECONDS
                    ) < 20
        }
        catch (e: CursorIndexOutOfBoundsException) {
            return null
        }
    }
}