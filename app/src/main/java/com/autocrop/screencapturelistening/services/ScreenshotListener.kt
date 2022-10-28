package com.autocrop.screencapturelistening.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
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
import com.autocrop.CropEdges
import com.autocrop.Screenshot
import com.autocrop.activities.cropping.cropping.cropEdges
import com.autocrop.activities.cropping.cropping.cropped
import com.autocrop.activities.iodetermination.CROP_FILE_ADDENDUM
import com.autocrop.activities.iodetermination.deleteRequestUri
import com.autocrop.screencapturelistening.abstractservices.BoundService
import com.autocrop.screencapturelistening.notifications.NotificationGroup
import com.autocrop.screencapturelistening.notifications.NotificationId
import com.autocrop.utils.android.extensions.compressToStream
import com.autocrop.utils.android.extensions.loadBitmap
import com.autocrop.utils.android.extensions.notificationBuilderWithSetChannel
import com.autocrop.utils.android.extensions.queryMediaStoreData
import com.autocrop.utils.android.systemScreenshotsDirectory
import com.autocrop.utils.kotlin.PendingIntentRenderer
import com.autocrop.utils.kotlin.dateFromUnixTimestamp
import com.autocrop.utils.kotlin.timeDelta
import com.google.common.collect.EvictingQueue
import com.w2sv.autocrop.R
import de.paul_woitaschek.slimber.i
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.concurrent.TimeUnit

class ScreenshotListener :
    BoundService(),
    OnPendingIntentService.ClientInterface by OnPendingIntentService.Client(0) {

    companion object {
        const val EXTRA_ATTEMPT_SCREENSHOT_DELETION = "com.autocrop.DELETE_SCREENSHOT"
        const val EXTRA_DELETE_REQUEST_URI = "com.autocrop.DELETE_REQUEST_URI"
        const val EXTRA_SCREENSHOT_MEDIASTORE_DATA = "com.autocrop.SCREENSHOT_MEDIASTORE_DATA"
        const val EXTRA_CROP_FILE_PATH = "com.autocrop.CROP_FILE_PATH"

        fun startService(context: Context) {
            with(context) {
                startService(
                    Intent(this, ScreenshotListener::class.java)
                )
            }
            i { "Started ScreenCaptureListeningService" }
        }

        fun stopService(context: Context) {
            with(context) {
                startService(
                    Intent(this, ScreenshotListener::class.java)
                        .setAction(ACTION_STOP_SERVICE)
                )
            }
            i { "Stopping ScreenCaptureListeningService" }
        }

        private const val ACTION_STOP_SERVICE = "com.autocrop.STOP_SERVICE"

        private val FOREGROUND_SERVICE_NOTIFICATION_ID = NotificationId.STARTED_FOREGROUND_SERVICE
    }

    /**
     * Starts foreground service with notification emission and registers [imageContentObserver]
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!!.action == ACTION_STOP_SERVICE) {
            stopService(Intent(this, OnPendingIntentService::class.java))

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        startForeground(
            FOREGROUND_SERVICE_NOTIFICATION_ID.id,
            foregroundServiceNotificationBuilder()
                .build()
        )
            .also { i { "Started foreground service" } }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            imageContentObserver
        )
            .also { i { "Registered imageContentObserver" } }

        return START_STICKY
    }

    private fun foregroundServiceNotificationBuilder(): NotificationCompat.Builder =
        notificationBuilderWithSetChannel(
            FOREGROUND_SERVICE_NOTIFICATION_ID.channelId,
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
                    PendingIntent.getBroadcast(
                        this,
                        999,
                        Intent(this, StopBroadcastReceiver::class.java),
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )

    class StopBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopService(context!!)
        }
    }

    @Suppress("UnstableApiUsage")
    private val imageContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
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
         * According to observation called on each change to a matching Uri, including
         * changes to their [MediaStore.MediaColumns].
         */
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            uri?.let {
                i { "Called onChange for $it" }
                if (!recentBlacklist.contains(it)) {
                    if (pendingScreenshotUris.contains(it) || it.isScreenshot() == true) {
                        if (onNewScreenshotUri(it)) {
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

    /**
     * Catches [IllegalStateException] in case of [uri] not being accessible due to still pending
     * (meaning that the device screenshot manager, := uri owner, is still accessing/processing the file,
     * such that the uri is blocked for other processes).
     *
     * @return [Boolean] indicating whether [uri] was accessible, meaning that this function should not
     * be called for the same [uri] again.
     */
    private fun onNewScreenshotUri(uri: Uri): Boolean =
        try {
            val bitmap = contentResolver.loadBitmap(uri)
            bitmap.cropEdges()?.let {
                showNewCroppableScreenshotDetectedNotification(uri, bitmap, it)
            }
            true
        }
        catch (_: IllegalStateException) {
            false
        }

    override val notificationGroup = NotificationGroup(
        this,
        "Detected croppable screenshots",
        NotificationId.DETECTED_NEW_CROPPABLE_SCREENSHOT,
        makeSummaryTitle = { "Detected $it croppable screenshots" },
        applyToSummaryBuilder = {
            it.setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText("Expand to save")
            )
        }
    )

    override fun onPendingIntentService(intent: Intent) {
        File(intent.getStringExtra(EXTRA_CROP_FILE_PATH)!!).delete()
    }

    private fun showNewCroppableScreenshotDetectedNotification(
        uri: Uri,
        screenshotBitmap: Bitmap,
        cropEdges: CropEdges
    ) {
        val notificationId = notificationGroup.children.newId()
        val associatedRequestCodes = requestCodes.makeAndAddMultiple(4)

        val screenshotMediaStoreData = Screenshot.MediaStoreData.query(contentResolver, uri)
        val deleteRequestUri = deleteRequestUri(screenshotMediaStoreData.id)
        val crop = screenshotBitmap.cropped(cropEdges)
        val cropPath = saveCropToTempFile(crop, screenshotMediaStoreData.id)

        fun pendingIntent(
            makePendingIntent: PendingIntentRenderer,
            intent: Intent,
            requestCodeIndex: Int,
            putCancelNotificationExtra: Boolean = true
        ): PendingIntent =
            makePendingIntent(
                this,
                associatedRequestCodes[requestCodeIndex],
                intent
                    .putExtra(EXTRA_CROP_FILE_PATH, cropPath)
                    .putOnPendingIntentServiceClientExtras(
                        notificationId,
                        associatedRequestCodes,
                        putCancelNotificationExtra
                    ),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

        fun Intent.putSaveIntentDataAndExtras() =
            this
                .setData(uri)
                .putExtra(EXTRA_SCREENSHOT_MEDIASTORE_DATA, screenshotMediaStoreData)

        notificationGroup.addChild(
            notificationId,
            notificationGroup.childBuilder("Fancy a crop?")
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Save",
                        pendingIntent(
                            PendingIntent::getService,
                            Intent(this, CropIOService::class.java)
                                .putSaveIntentDataAndExtras(),
                            0
                        )
                    )
                )
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Save & delete",
                        if (deleteRequestUri is Uri && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                            pendingIntent(
                                PendingIntent::getActivity,
                                Intent(this, DeleteRequestActivity::class.java)
                                    .putSaveIntentDataAndExtras()
                                    .putExtra(EXTRA_DELETE_REQUEST_URI, deleteRequestUri),
                                1
                            )
                        else
                            pendingIntent(
                                PendingIntent::getService,
                                Intent(this, CropIOService::class.java)
                                    .putSaveIntentDataAndExtras()
                                    .putExtra(EXTRA_ATTEMPT_SCREENSHOT_DELETION, true),
                                1
                            )
                    )
                )
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Dismiss",
                        pendingIntent(
                            PendingIntent::getService,
                            Intent(this, OnPendingIntentService::class.java),
                            2
                        )
                    )
                )
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(crop)
                )
                .setDeleteIntent(
                    pendingIntent(
                        PendingIntent::getService,
                        Intent(this, OnPendingIntentService::class.java),
                        3,
                        false
                    )
                )
        )
    }

    private fun saveCropToTempFile(crop: Bitmap, screenshotMediaStoreId: Long): String {
        val file = File.createTempFile(
            screenshotMediaStoreId.toString(),
            null
        )
        crop.compressToStream(
            FileOutputStream(file),
            Bitmap.CompressFormat.PNG
        )
        return file.path
    }

    override fun onDestroy() {
        super.onDestroy()

        stopService(Intent(this, CropIOService::class.java))

        contentResolver.unregisterContentObserver(imageContentObserver)
            .also { i { "Unregistered imageContentObserver" } }
    }
}