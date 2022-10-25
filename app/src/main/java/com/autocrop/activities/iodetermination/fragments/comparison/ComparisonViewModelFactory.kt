package com.autocrop.activities.iodetermination.fragments.comparison

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.CropBundle

class ComparisonViewModelFactory(private val cropBundle: CropBundle, private val screenshotBitmap: Bitmap)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        modelClass.getConstructor(CropBundle::class.java, Bitmap::class.java)
            .newInstance(cropBundle, screenshotBitmap)
}