package com.autocrop.screencapturelistening

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.autocrop.activities.iodetermination.CROP_FILE_ADDENDUM
import com.autocrop.utils.android.extensions.queryMediaStoreColumns
import com.autocrop.utils.android.extensions.showNotification

class ScreenCaptureListener(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    companion object{
        const val SCREENSHOT_URI_EXTRA_KEY = "SCREENSHOT_URI_EXTRA_KEY"
        const val TAG = "SCREENSHOT_CAPTURE_LISTENER"
    }

    override fun doWork(): Result {
        createContentObserverFlow()
        return Result.success()
    }

    private fun createContentObserverFlow(){
        applicationContext.contentResolver?.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
            )
    }

    private val contentObserver by lazy {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
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
    }

    private fun onNewImageUriFound(uri: Uri) {
        val mediaStoreData = applicationContext.contentResolver.queryMediaStoreColumns(
            uri,
            arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA
            )
        )

        val absolutePath = mediaStoreData[0]
        val fileName = mediaStoreData[1]

        if (isScreenshot(absolutePath, fileName)){
            showNewScreenshotDetectedNotification(uri)
        }
    }

    /**
     * /storage/emulated/0/Pictures/Screenshots/.pending-1665749333-Screenshot_20221007-140853687.png
     */
    private fun isScreenshot(absolutePath: String, name: String): Boolean {
        return !name.contains(CROP_FILE_ADDENDUM) &&
                publicScreenshotDirectoryName()?.let {
                    absolutePath.contains(it)
                } == true ||
                name.lowercase().contains("screenshot")
    }

    private fun publicScreenshotDirectoryName() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS).name
        else
            null

    private fun showNewScreenshotDetectedNotification(uri: Uri){
        applicationContext.showNotification(
            "DETECTED_NEW_SCREENSHOT",
            "Detected new screenshot",
            "New screenshot detected",
            "Fancy an AutoCrop?",
            NotificationId.detectedNewScreenshot,
            NotificationCompat.Action(
                null,
                "Do crop",
                PendingIntent.getService(
                    applicationContext,
                    -1,
                    Intent(applicationContext, CropService::class.java)
                        .putExtra(
                            SCREENSHOT_URI_EXTRA_KEY,
                            uri
                        ),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        )
    }

    override fun onStopped() {
        super.onStopped()

        applicationContext.contentResolver?.unregisterContentObserver(
            contentObserver
        )
    }
}