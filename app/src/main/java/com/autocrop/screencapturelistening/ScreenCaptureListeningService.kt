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
import com.autocrop.utils.android.extensions.queryMediaStoreColumns
import com.autocrop.utils.android.extensions.showNotification
import com.autocrop.utils.kotlin.extensions.nonZeroOrdinal
import com.lyrebirdstudio.croppylib.CropEdges
import timber.log.Timber

class ScreenCaptureListeningService: Service() {

    companion object{
        const val SCREENSHOT_URI_EXTRA_KEY = "SCREENSHOT_URI_EXTRA_KEY"
        const val CROP_EDGES_EXTRA_KEY = "CROP_EDGES_EXTRA_KEY"
        const val CLOSE_NOTIFICATION_ID_EXTRA_KEY = "CLOSE_NOTIFICATION_ID_EXTRA_KEY"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationId.STARTED_FOREGROUND_SERVICE.nonZeroOrdinal,
            notificationBuilderWithSetChannel(
                NotificationId.STARTED_FOREGROUND_SERVICE,
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

    private val imageContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        var previousUri: Uri? = null
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            uri?.let {
                if (it != previousUri){
                    onNewImageUriFound(it)
                    previousUri = it
                }
            }
        }
    }

    private fun onNewImageUriFound(uri: Uri) {
        val mediaStoreData = contentResolver.queryMediaStoreColumns(
            uri,
            arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA
            )
        )

        val absolutePath = mediaStoreData[0]
        val fileName = mediaStoreData[1]

        if (isScreenshot(absolutePath, fileName)){
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    val bitmap = contentResolver.openBitmap(uri)
                    bitmap.cropEdges()?.let {
                        showNewCroppableScreenshotDetectedNotification(uri, bitmap, it)
                    }
                },
                2000
            )
        }
    }

    /**
     * /storage/emulated/0/Pictures/Screenshots/.pending-1665749333-Screenshot_20221007-140853687.png
     */
    private fun isScreenshot(absolutePath: String, name: String): Boolean =
        !name.contains(CROP_FILE_ADDENDUM) &&
        (publicScreenshotDirectoryName()?.let {
            absolutePath.contains(it)
        } == true ||
        name.lowercase().contains("screenshot"))

    private fun publicScreenshotDirectoryName() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS).name
        else
            null

    private fun showNewCroppableScreenshotDetectedNotification(uri: Uri, screenshotBitmap: Bitmap, cropEdges: CropEdges){
        val notificationId = NotificationId.DETECTED_NEW_CROPPABLE_SCREENSHOT
        showNotification(
            notificationId,
            notificationBuilderWithSetChannel(
                notificationId,
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
                            .putExtra(CLOSE_NOTIFICATION_ID_EXTRA_KEY, notificationId),
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(screenshotBitmap.cropped(cropEdges))
                )
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        contentResolver.unregisterContentObserver(imageContentObserver)
            .also { Timber.i("Unregistered imageContentObserver") }
    }
}