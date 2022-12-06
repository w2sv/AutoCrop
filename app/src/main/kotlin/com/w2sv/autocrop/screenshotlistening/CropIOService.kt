package com.w2sv.autocrop.screenshotlistening

import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import com.w2sv.autocrop.cropbundle.io.CropBundleIOResult
import com.w2sv.autocrop.cropbundle.io.CropBundleIORunner
import com.w2sv.autocrop.screenshotlistening.services.abstrct.UnboundService
import com.w2sv.autocrop.utils.extensions.getParcelable

/**
 * Service responsible for carrying out crop bundle IO and notifying the user
 * as to the respective result
 */
class CropIOService : UnboundService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ScreenshotListener.startCleanupService(this, intent!!)

        val ioResult = runIntentParametrizedCropBundleIO(intent)
        showIOResultsNotification(ioResult)

        stopSelf()

        return START_REDELIVER_INTENT
    }

    private fun runIntentParametrizedCropBundleIO(intent: Intent): CropBundleIOResult =
        CropBundleIORunner.getInstance(applicationContext).invoke(
            cropBitmap = BitmapFactory.decodeFile(intent.getStringExtra(ScreenshotListener.EXTRA_TEMPORARY_CROP_FILE_PATH)),
            screenshotMediaStoreData = intent.getParcelable(ScreenshotListener.EXTRA_SCREENSHOT_MEDIASTORE_DATA)!!,
            deleteScreenshot = intent.getBooleanExtra(ScreenshotListener.EXTRA_ATTEMPT_SCREENSHOT_DELETION, false)
        )
            .apply {
                if (intent.getBooleanExtra(DeleteRequestActivity.EXTRA_CONFIRMED_DELETION, false))
                    deletedScreenshot = true
            }

    private fun showIOResultsNotification(cropBundleIoResult: CropBundleIOResult) {
        Toast.makeText(
            applicationContext,
            when {
                cropBundleIoResult.successfullySavedCrop -> buildString {
                    append("Successfully saved crop")
                    if (cropBundleIoResult.deletedScreenshot == true)
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