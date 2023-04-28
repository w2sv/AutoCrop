package com.w2sv.autocrop.domain

import android.net.Uri
import android.os.Parcelable
import androidx.core.text.buildSpannedString
import com.w2sv.cropbundle.io.CropBundleIOResult
import com.w2sv.kotlinutils.extensions.numericallyInflected
import com.w2sv.kotlinutils.extensions.toInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccumulatedIOResults(
    var cropUris: ArrayList<Uri> = ArrayList(),
    var nDeletedScreenshots: Int = 0
) : Parcelable {

    companion object {
        const val EXTRA = "com.w2sv.autocrop.extra.IO_RESULTS"
    }

    private val nSavedCrops: Int get() = cropUris.size

    val anyCropsSaved: Boolean get() = cropUris.isNotEmpty()

    fun addFrom(ioResult: CropBundleIOResult) {
        ioResult.cropUri?.let {
            cropUris.add(it)
        }
        ioResult.deletedScreenshot?.let {
            nDeletedScreenshots += it.toInt()
        }
    }

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
}