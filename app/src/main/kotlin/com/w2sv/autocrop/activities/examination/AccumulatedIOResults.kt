package com.w2sv.autocrop.activities.examination

import android.net.Uri
import android.os.Parcelable
import com.w2sv.cropbundle.io.CropBundleIOResult
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

    val nSavedCrops: Int get() = cropUris.size

    fun addFrom(ioResult: CropBundleIOResult) {
        ioResult.cropUri?.let {
            cropUris.add(it)
        }
        ioResult.deletedScreenshot?.let {
            nDeletedScreenshots += it.toInt()
        }
    }
}