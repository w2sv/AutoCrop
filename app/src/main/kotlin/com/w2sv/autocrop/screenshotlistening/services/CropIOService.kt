package com.w2sv.autocrop.screenshotlistening.services

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import com.w2sv.autocrop.cropbundle.Screenshot
import com.w2sv.autocrop.cropbundle.io.CropBundleIOProcessor
import com.w2sv.autocrop.cropbundle.io.IOResult
import com.w2sv.autocrop.screenshotlistening.services.abstrct.BoundService
import com.w2sv.autocrop.utils.extensions.getParcelable

class CropIOService :
    BoundService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!) {
            startService(
                setClass(this@CropIOService, OnPendingIntentService::class.java)
            )

            val ioResult = carryOutCropIO(
                crop = BitmapFactory.decodeFile(getStringExtra(ScreenshotListener.EXTRA_CROP_FILE_PATH)),
                screenshotMediaStoreData = getParcelable(ScreenshotListener.EXTRA_SCREENSHOT_MEDIASTORE_DATA)!!,
                deleteScreenshot = getBooleanExtra(ScreenshotListener.EXTRA_ATTEMPT_SCREENSHOT_DELETION, false)
            )
            if (getBooleanExtra(DeleteRequestActivity.EXTRA_CONFIRMED_DELETION, false))
                ioResult.deletedScreenshot = true

            showNotification(ioResult)
        }
        return START_REDELIVER_INTENT
    }

    private fun carryOutCropIO(
        crop: Bitmap,
        screenshotMediaStoreData: Screenshot.MediaStoreData,
        deleteScreenshot: Boolean
    ): IOResult =
        CropBundleIOProcessor.getInstance(applicationContext).invoke(
            crop,
            screenshotMediaStoreData,
            deleteScreenshot
        )

    private fun showNotification(ioResult: IOResult) {
        Toast.makeText(
            applicationContext,
            when {
                ioResult.successfullySavedCrop -> buildString {
                    append("Successfully saved crop")
                    if (ioResult.deletedScreenshot == true)
                        append(" & deleted Screenshot")
                    append("!")
                }

                else -> "Couldn't save crop"
            },
            Toast.LENGTH_LONG
        )
            .show()
    }
}