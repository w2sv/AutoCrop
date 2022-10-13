package com.autocrop.screencapturelistening.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.autocrop.activities.cropping.cropping.cropEdges
import com.autocrop.activities.cropping.cropping.cropped
import com.autocrop.activities.iodetermination.CROP_FILE_ADDENDUM
import com.autocrop.activities.iodetermination.deleteRequestUri
import com.autocrop.dataclasses.Screenshot
import com.autocrop.screencapturelistening.abstractservices.BoundService
import com.autocrop.screencapturelistening.notifications.NotificationGroup
import com.autocrop.screencapturelistening.notifications.NotificationId
import com.autocrop.utils.android.extensions.notificationBuilderWithSetChannel
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.android.extensions.queryMediaStoreData
import com.autocrop.utils.android.systemScreenshotsDirectory
import com.autocrop.utils.kotlin.dateFromUnixTimestamp
import com.autocrop.utils.kotlin.timeDelta
import com.google.common.collect.EvictingQueue
import com.lyrebirdstudio.croppylib.CropEdges
import com.w2sv.autocrop.R
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit

class ScreenCaptureListeningService :
    BoundService(),
    OnPendingIntentService.ClientInterface by OnPendingIntentService.Client(0) {

    companion object {
        const val EXTRA_ATTEMPT_SCREENSHOT_DELETION = "com.autocrop.DELETE_SCREENSHOT"
        const val EXTRA_DELETE_REQUEST_URI = "com.autocrop.DELETE_REQUEST_URI"
        const val EXTRA_SCREENSHOT_MEDIASTORE_DATA = "com.autocrop.SCREENSHOT_MEDIASTORE_DATA"

        private val FOREGROUND_SERVICE_NOTIFICATION_ID = NotificationId.STARTED_FOREGROUND_SERVICE
    }

    /**
     * Starts foreground service with notification emission and registers [imageContentObserver]
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            FOREGROUND_SERVICE_NOTIFICATION_ID.id,
            foregroundServiceNotificationBuilder()
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

    private fun foregroundServiceNotificationBuilder(): NotificationCompat.Builder =
        notificationBuilderWithSetChannel(
            FOREGROUND_SERVICE_NOTIFICATION_ID.channelId,
            "Listening to screen captures"
        )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You will receive a notification when AutoCrop detects a new croppable screenshot")
            )
            .setSilent(true)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_cancel_24,
                    "Stop",
                    PendingIntent.getBroadcast(
                        this,
                        69,
                        Intent(this, StoppingBroadcastReceiver::class.java),
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )

    class StoppingBroadcastReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            context!!.stopService(
                Intent(context, ScreenCaptureListeningService::class.java)
            )
        }
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
            .also { CropIOService.cropBitmap = it }

        notificationGroup.addChild(
            notificationId,
            notificationGroup.childBuilder("Fancy a crop?")
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Save",
                        PendingIntent.getService(
                            this,
                            associatedRequestCodes[0],
                            Intent(this, CropIOService::class.java)
                                .setData(uri)
                                .putExtra(EXTRA_SCREENSHOT_MEDIASTORE_DATA, screenshotMediaStoreData)
                                .putExtra(OnPendingIntentService.EXTRA_CANCEL_NOTIFICATION, true)
                                .putClientExtras(notificationId, associatedRequestCodes),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                )
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "...& delete screenshot",
                        if (deleteRequestUri is Uri && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                            PendingIntent.getActivity(
                                this,
                                associatedRequestCodes[1],
                                Intent(this, DeleteRequestActivity::class.java)
                                    .setData(uri)
                                    .putExtra(EXTRA_SCREENSHOT_MEDIASTORE_DATA, screenshotMediaStoreData)
                                    .putExtra(EXTRA_DELETE_REQUEST_URI, deleteRequestUri)
                                    .putExtra(OnPendingIntentService.EXTRA_CANCEL_NOTIFICATION, true)
                                    .putClientExtras(notificationId, associatedRequestCodes),
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        else
                            PendingIntent.getService(
                                this,
                                associatedRequestCodes[1],
                                Intent(this, CropIOService::class.java)
                                    .setData(uri)
                                    .putExtra(EXTRA_SCREENSHOT_MEDIASTORE_DATA, screenshotMediaStoreData)
                                    .putExtra(OnPendingIntentService.EXTRA_CANCEL_NOTIFICATION, true)
                                    .putExtra(EXTRA_ATTEMPT_SCREENSHOT_DELETION, true)
                                    .putClientExtras(notificationId, associatedRequestCodes),
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                    )
                )
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Dismiss",
                        PendingIntent.getService(
                            this,
                            associatedRequestCodes[2],
                            Intent(this, OnPendingIntentService::class.java)
                                .putExtra(OnPendingIntentService.EXTRA_CANCEL_NOTIFICATION, true)
                                .putClientExtras(notificationId, associatedRequestCodes),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                )
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(crop)
                )
                .setDeleteIntent(
                    PendingIntent.getService(
                        this,
                        associatedRequestCodes[3],
                        Intent(this, OnPendingIntentService::class.java)
                            .putClientExtras(notificationId, associatedRequestCodes),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
        )
    }

    override fun onPendingIntentService(intent: Intent) {
        CropIOService.cropBitmap = null
    }

    override fun onDestroy() {
        super.onDestroy()

        contentResolver.unregisterContentObserver(imageContentObserver)
            .also { Timber.i("Unregistered imageContentObserver") }

        stopService(Intent(this, CropIOService::class.java))
        stopService(Intent(this, OnPendingIntentService::class.java))
    }
}