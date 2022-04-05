package com.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.types.CropBundleList

class CroppingActivityViewModel(val uris: ArrayList<Uri>): ViewModel(){
    companion object{
        const val PROGRESS_BAR_MAX: Int = 100
    }

    val progressBarIntStep: Int
    val progressBarDecimalStep: Float

    val nSelectedImages: Int = uris.size
    val cropBundles: CropBundleList = mutableListOf()

    val nDismissedImages: Int
        get() = nSelectedImages - cropBundles.size

    init {
        (PROGRESS_BAR_MAX.toFloat() / nSelectedImages.toFloat()).let {
            progressBarIntStep = it.toInt()
            progressBarDecimalStep = it - progressBarIntStep
        }
    }
}