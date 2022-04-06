package com.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.types.CropBundle

class CroppingActivityViewModel(val uris: ArrayList<Uri>): ViewModel(){

    val progressBarIntStep: Int
    val progressBarDecimalStep: Float

    val nSelectedImages: Int = uris.size
    val cropBundles: MutableList<CropBundle> = mutableListOf()

    val nDismissedImages: Int
        get() = nSelectedImages - cropBundles.size

    init {
        val progressBarMax = 100f

        (progressBarMax / nSelectedImages.toFloat()).let {
            progressBarIntStep = it.toInt()
            progressBarDecimalStep = it - progressBarIntStep
        }
    }
}