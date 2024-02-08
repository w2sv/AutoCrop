package com.w2sv.autocrop.activities.examination

import android.net.Uri
import android.os.Parcelable
import androidx.core.text.buildSpannedString
import com.w2sv.cropbundle.io.CropBundleIOResult
import com.w2sv.cropbundle.io.ScreenshotDeletionResult
import com.w2sv.kotlinutils.extensions.numericallyInflected
import kotlinx.parcelize.Parcelize

@Parcelize
data class IOResults(
    val cropUris: ArrayList<Uri>,
    val nDeletedScreenshots: Int
) : Parcelable {

    private val nSavedCrops: Int
        get() = cropUris.size

    val anyCropsSaved: Boolean
        get() = cropUris.isNotEmpty()

    fun getNotificationText(): CharSequence =
        if (nSavedCrops == 0)
            "Discarded all crops"
        else
            buildSpannedString {
                append(
                    "Saved $nSavedCrops ${"crop".numericallyInflected(nSavedCrops)}"
                )
                if (nDeletedScreenshots != 0)
                    append(
                        " and deleted ${
                            if (nDeletedScreenshots == nSavedCrops)
                                "corresponding"
                            else
                                nDeletedScreenshots
                        } ${"screenshot".numericallyInflected(nDeletedScreenshots)}"
                    )
            }

    companion object {
        const val EXTRA = "com.w2sv.autocrop.extra.IO_RESULTS"

        fun get(ioResults: Iterable<CropBundleIOResult>): IOResults {
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

            return IOResults(cropUris, nDeletedScreenshots)
        }
    }
}