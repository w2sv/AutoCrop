package com.autocrop.screencapturelistening

import android.app.PendingIntent
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.autocrop.activities.cropping.cropping.cropEdges
import com.autocrop.activities.cropping.cropping.cropped
import com.autocrop.activities.iodetermination.CROP_FILE_ADDENDUM
import com.autocrop.screencapturelistening.notification.ASSOCIATED_NOTIFICATION_ID
import com.autocrop.screencapturelistening.notification.CANCEL_NOTIFICATION_ACTION
import com.autocrop.screencapturelistening.notification.NotificationGroup
import com.autocrop.screencapturelistening.notification.NotificationId
import com.autocrop.screencapturelistening.abstractservices.BoundService
import com.autocrop.screencapturelistening.notification.ASSOCIATED_PENDING_REQUEST_CODES
import com.autocrop.screencapturelistening.notification.ScopeWideUniqueIds
import com.autocrop.utils.android.extensions.notificationBuilderWithSetChannel
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.android.extensions.queryMediaStoreData
import com.autocrop.utils.android.systemScreenshotsDirectory
import com.google.common.collect.EvictingQueue
import com.lyrebirdstudio.croppylib.CropEdges
import timber.log.Timber

class ScreenCaptureListeningService : BoundService() {

    companion object {
        const val CROP_EDGES_EXTRA_KEY = "CROP_EDGES_EXTRA_KEY"
    }

    /**
     * Starts foreground service with notification emission and registers [imageContentObserver]
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationId.STARTED_FOREGROUND_SERVICE.id,
            notificationBuilderWithSetChannel(
                NotificationId.STARTED_FOREGROUND_SERVICE.channelId,
                "Listening to screen captures"
            )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("You will receive a notification when AutoCrop detects a new croppable screenshot")
                )
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
                if (!recentBlacklist.contains(it)) {
                    if (pendingScreenshotUris.contains(it) || it.isScreenshot()) {
                        if (onNewScreenshotUri(it)) {
                            recentBlacklist.add(it)
                            pendingScreenshotUris.remove(it)
                            Timber.i("Added $uri to blacklist and removed it from pendingScreenshotUris")
                        } else
                            pendingScreenshotUris.add(it)
                                .also {
                                    Timber.i(
                                        "Added $uri to pendingScreenshotUris"
                                    )
                                }
                    } else
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
     * [systemScreenshotsDirectory]name or the word 'screenshot'.
     */
    private fun Uri.isScreenshot(): Boolean {
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
                (systemScreenshotsDirectory()?.let {
                    absolutePath.contains(it.name)
                } == true || fileName.lowercase().contains("screenshot"))
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
            val bitmap = contentResolver.openBitmap(uri)
            bitmap.cropEdges()?.let {
                showNewCroppableScreenshotDetectedNotification(uri, bitmap, it)
            }
            true
        } catch (_: IllegalStateException) {
            false
        }

    val notificationGroup = NotificationGroup(
        this,
        NotificationId.DETECTED_NEW_CROPPABLE_SCREENSHOT,
        makeSummaryTitle = { "Detected $it croppable screenshots" },
        applyToSummaryBuilder = {
            it.setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText("Expand to save")
            )
        }
    )
    val cancellationRequestCodes = ScopeWideUniqueIds()

    private fun showNewCroppableScreenshotDetectedNotification(
        uri: Uri,
        screenshotBitmap: Bitmap,
        cropEdges: CropEdges
    ) {
        val dynamicChildId = notificationGroup.children.newId()
        val pendingRequestCodes = intArrayOf(
            cancellationRequestCodes.addNewId(),
            cancellationRequestCodes.addNewId()
        )

        notificationGroup.addAndShowChild(
            dynamicChildId,
            notificationGroup.childBuilder("Detected new croppable screenshot")
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Save crop",
                        PendingIntent.getService(
                            this,
                            pendingRequestCodes[0],
                            Intent(this, CropIOService::class.java)
                                .setData(uri)
                                .setAction(CANCEL_NOTIFICATION_ACTION)
                                .putExtra(CROP_EDGES_EXTRA_KEY, cropEdges)
                                .putExtra(ASSOCIATED_NOTIFICATION_ID, dynamicChildId)
                                .putExtra(ASSOCIATED_PENDING_REQUEST_CODES, pendingRequestCodes),
                            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                )
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(screenshotBitmap.cropped(cropEdges))
                )
                .setDeleteIntent(
                    PendingIntent.getService(
                        this,
                        pendingRequestCodes[1],
                        Intent(
                            this,
                            NotificationCancellationService::class.java
                        )
                            .putExtra(ASSOCIATED_NOTIFICATION_ID, dynamicChildId)
                            .putExtra(ASSOCIATED_PENDING_REQUEST_CODES, pendingRequestCodes),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                    )
                )
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        contentResolver.unregisterContentObserver(imageContentObserver)
            .also { Timber.i("Unregistered imageContentObserver") }

        stopService(Intent(this, CropIOService::class.java))
        stopService(Intent(this, NotificationCancellationService::class.java))
    }
}