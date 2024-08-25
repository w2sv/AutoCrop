package com.w2sv.autocrop.model

import android.content.res.Resources
import android.net.Uri
import android.os.Parcelable
import androidx.core.text.buildSpannedString
import com.w2sv.autocrop.R
import com.w2sv.cropbundle.io.CropBundleIOResult
import com.w2sv.cropbundle.io.ScreenshotDeletionResult
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropBundleIOResults(
    val cropUris: ArrayList<Uri>,
    val deletedScreenshotCount: Int
) : Parcelable {

    private val nSavedCrops: Int
        get() = cropUris.size

    val anyCropsSaved: Boolean
        get() = cropUris.isNotEmpty()

    fun getNotificationText(resources: Resources): CharSequence =
        if (nSavedCrops == 0)
            "Discarded all crops"
        else
            buildSpannedString {
                append(
                    "Saved $nSavedCrops ${resources.getQuantityString(R.plurals.crop, nSavedCrops)}"
                )
                if (deletedScreenshotCount != 0)
                    append(
                        " and deleted ${
                            if (deletedScreenshotCount == nSavedCrops)
                                "corresponding"
                            else
                                deletedScreenshotCount
                        } ${resources.getQuantityString(R.plurals.screenshot, deletedScreenshotCount)}"
                    )
            }

    companion object {
        const val EXTRA = "com.w2sv.autocrop.extra.IO_RESULTS"

        fun get(ioResults: Iterable<CropBundleIOResult>): CropBundleIOResults {
            val cropUris = ArrayList<Uri>()
            var nDeletedScreenshots = 0

            ioResults.forEach {
                it.cropFileUri?.let { uri ->
                    cropUris.add(uri)
                }
                if (it.screenshotDeletionResult == ScreenshotDeletionResult.SuccessfullyDeleted) {
                    nDeletedScreenshots += 1
                }
            }

            return CropBundleIOResults(cropUris, nDeletedScreenshots)
        }
    }
}