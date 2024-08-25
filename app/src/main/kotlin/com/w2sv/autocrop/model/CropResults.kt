package com.w2sv.autocrop.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropResults(var nNotOpenableImages: Int = 0, var uncroppableImageCount: Int = 0) : Parcelable {

    companion object {
        const val EXTRA = "com.w2sv.autocrop.extra.CROP_RESULTS"
    }
}