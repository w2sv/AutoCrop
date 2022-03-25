package com.autocrop.activities.cropping

import androidx.lifecycle.ViewModel

class CroppingActivityViewModel(val nSelectedImages: Int, progressBarMax: Int): ViewModel(){
    val progressBarIntStep: Int
    val progressBarDecimalStep: Float

    init {
        (progressBarMax.toFloat() / nSelectedImages.toFloat()).let {
            progressBarIntStep = it.toInt()
            progressBarDecimalStep = it - progressBarIntStep
        }
    }
}