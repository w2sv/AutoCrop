package com.autocrop.screencapturelistening

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.autocrop.activities.cropping.cropping.cropEdges
import com.autocrop.activities.cropping.cropping.cropped
import com.autocrop.activities.iodetermination.CROP_FILE_ADDENDUM
import com.autocrop.utils.android.extensions.notificationBuilderWithSetChannel
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.android.extensions.queryMediaStoreData
import com.autocrop.utils.android.extensions.showGroupUpdatedNotification
import com.autocrop.utils.android.extensions.showNotification
import com.autocrop.utils.kotlin.extensions.nonZeroOrdinal
import com.google.common.collect.EvictingQueue
import com.lyrebirdstudio.croppylib.CropEdges
import timber.log.Timber

const val NOTIFICATION_ID_EXTRA_KEY = "CLOSE_NOTIFICATION_ID_EXTRA_KEY"

class ScreenCaptureListeningService: Service() {

    companion object{
        const val SCREENSHOT_URI_EXTRA_KEY = "SCREENSHOT_URI_EXTRA_KEY"
        const val CROP_EDGES_EXTRA_KEY = "CROP_EDGES_EXTRA_KEY"

        val groupNotificationId = NotificationId.DETECTED_NEW_CROPPABLE_SCREENSHOT
        val ids = DynamicNotificationIds(groupNotificationId)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationId.STARTED_FOREGROUND_SERVICE.nonZeroOrdinal,
            notificationBuilderWithSetChannel(
                NotificationId.STARTED_FOREGROUND_SERVICE.name,
                "Listening to screen captures"
            )
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("You will receive a notification when AutoCrop detects a new croppable screenshot"))
                .build()
        )
            .also { Timber.i("Started ScreenCaptureListeningService in foreground") }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            imageContentObserver
        )
            .also { Timber.i("Registered imageContentObserver") }

        return START_STICKY
    }

    @Suppress("UnstableApiUsage")  // EvictingQueue part of Beta-API
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
                Timber.i("Called onChange for $it")
                if (!recentBlacklist.contains(it)){
                    if (pendingScreenshotUris.contains(it) || it.isScreenshot()){
                        if (onNewScreenshotUri(it)) {
                            recentBlacklist.add(it)
                            pendingScreenshotUris.remove(it)
                            Timber.i("Added $uri to blacklist and removed it from pendingScreenshotUris")
                        }
                        else
                            pendingScreenshotUris.add(it)
                                .also {
                                    Timber.i(
                                        "Added $uri to pendingScreenshotUris"
                                    )
                                }
                    }
                    else
                        recentBlacklist.add(it)
                            .also {
                                Timber.i("Added $uri to blacklist")
                            }
                }
            }
        }
    }

    /**
     * Checks if [this] not corresponding to AutoCrop-file and if its file path contains either the
     * [publicScreenshotDirectoryName] or the word 'screenshot'.
     */
    private fun Uri.isScreenshot(): Boolean{
        val mediaStoreData = contentResolver.queryMediaStoreData(
            this,
            arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA  // e.g. /storage/emulated/0/Pictures/Screenshots/.pending-1665749333-Screenshot_20221007-140853687.png
            )
        )

        val absolutePath = mediaStoreData[0]
        val fileName = mediaStoreData[1]

        return !fileName.contains(CROP_FILE_ADDENDUM) &&
                (publicScreenshotDirectoryName()?.let {
                    absolutePath.contains(it)
                } == true || fileName.lowercase().contains("screenshot"))
    }

    /**
     * @return e.g. 'Screenshots'
     */
    private fun publicScreenshotDirectoryName(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS).name
        else
            null

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
            val bitmap = contentResolver.openBitmap(uri)
            bitmap.cropEdges()?.let {
                showNewCroppableScreenshotDetectedNotification(uri, bitmap, it)
            }
            true
        }
        catch (_: IllegalStateException){
            false
        }

    private fun showNewCroppableScreenshotDetectedNotification(uri: Uri, screenshotBitmap: Bitmap, cropEdges: CropEdges){
        if (ids.size == 1){
            val (id, builder) = ids.element()
            showGroupUpdatedNotification(
                id,
                builder,
                groupNotificationId.groupKey
            )
        }

        val id = ids.newId()
        val builder = notificationBuilderWithSetChannel(
            ids.channelId,
            "Detected new croppable screenshot",
            action = NotificationCompat.Action(
                null,
                "Save crop",
                PendingIntent.getService(
                    this,
                    PendingIntentRequestCode.CROP_IO_SERVICE.ordinal,
                    Intent(this, CropIOService::class.java)
                        .putExtra(SCREENSHOT_URI_EXTRA_KEY, uri)
                        .putExtra(CROP_EDGES_EXTRA_KEY, cropEdges)
                        .putExtra(NOTIFICATION_ID_EXTRA_KEY, id),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        )
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(screenshotBitmap.cropped(cropEdges))
            )
            .setGroup(groupNotificationId.groupKey)
            .setOnlyAlertOnce(true)
            .setDeleteIntent(
                PendingIntent.getService(
                    this,
                    PendingIntentRequestCode.NOTIFICATION_CANCELLATION_LISTENER_SERVICE.ordinal,
                    Intent(
                        this,
                        NotificationCancellationListenerService::class.java
                    )
                        .putExtra(NOTIFICATION_ID_EXTRA_KEY, id),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        ids.add(id to builder)
        showNotification(
            id,
            builder
        )

        // Show notification group summary
        if (ids.size >= 2){
            showNotification(
                groupNotificationId.nonZeroOrdinal,
                notificationBuilderWithSetChannel(
                    ids.channelId,
                    "Detected ${ids.size} croppable screenshots"
                )
                    .setStyle(NotificationCompat.InboxStyle()
                        .setBigContentTitle("Detected ${ids.size} croppable screenshots")
                        .setSummaryText("Expand to save"))
                    .setGroup(groupNotificationId.groupKey)
                    .setGroupSummary(true)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        contentResolver.unregisterContentObserver(imageContentObserver)
            .also { Timber.i("Unregistered imageContentObserver") }
    }
}