package com.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.types.CropBundle
import com.autocrop.utils.toInt

class CroppingActivityViewModel(val uris: ArrayList<Uri>): ViewModel(){

    private val progressBarIntStep: Int
    private val progressBarDecimalStep: Float

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

    private var decimalStepSum = 0f

    fun incrementDecimalStepSum(): Int{
        decimalStepSum += progressBarDecimalStep
        return progressBarIntStep + (decimalStepSum >= 1).toInt()
            .also {
                if (it > progressBarDecimalStep)
                    decimalStepSum -= 1
            }
    }
}