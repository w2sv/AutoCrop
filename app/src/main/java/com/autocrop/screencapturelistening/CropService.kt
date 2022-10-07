package com.autocrop.screencapturelistening

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import com.autocrop.activities.cropping.cropping.cropEdges
import com.autocrop.activities.iodetermination.processCropBundle
import com.autocrop.dataclasses.CropBundle
import com.autocrop.dataclasses.Screenshot
import com.autocrop.preferences.UriPreferences
import com.autocrop.utils.android.extensions.cancelNotification
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.android.extensions.showNotification

class CropService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        cancelNotification(NotificationId.detectedNewScreenshot)

        val successfullySaved = applicationContext.cropAndSave(
            @Suppress("DEPRECATION") intent?.getParcelableExtra(ScreenCaptureListener.SCREENSHOT_URI_EXTRA_KEY)!!,
        )
        if (successfullySaved)
            showNotification(
                "SUCCESSFULLY_SAVED_CROP",
                "Successfully saved crop",
                "Successfully saved crop",
                "INSERT WRITE PATH",
                NotificationId.successfullySavedCrop
            )
        else
            showNotification(
                "NO_CROP_EDGES_FOUND",
                "No crop edges found",
                "No crop edges found",
                "Tap to crop manually",
                NotificationId.noCropEdgesFound
            )

        return super.onStartCommand(intent, flags, startId)
    }

    private fun Context.cropAndSave(screenshotUri: Uri): Boolean {
        val bitmap = contentResolver.openBitmap(screenshotUri)
        bitmap.cropEdges()?.let {
            val (savingResult, successfullyDeleted) = processCropBundle(
                CropBundle.assemble(
                    Screenshot(
                        screenshotUri,
                        -1,
                        listOf(),
                        Screenshot.MediaStoreColumns.query(
                            contentResolver,
                            screenshotUri
                        )
                    ),
                    bitmap,
                    it
                ),
                UriPreferences.documentUri,
                false
            )
            return savingResult.first
        }
        return false
    }
}