package com.w2sv.autocrop.activities.crop

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropResults(var nNotOpenableImages: Int = 0, var nNotCroppableImages: Int = 0) : Parcelable{

    companion object{
        const val EXTRA = "com.w2sv.autocrop.EXTRA_CROP_RESULTS"
    }
}