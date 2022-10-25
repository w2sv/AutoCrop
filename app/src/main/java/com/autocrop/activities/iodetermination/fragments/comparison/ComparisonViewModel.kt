package com.autocrop.activities.iodetermination.fragments.comparison

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.CropBundle

class ComparisonViewModel(val cropBundle: CropBundle, val screenshotBitmap: Bitmap): ViewModel(){
    var enterTransitionCompleted = false

    val displayScreenshot: LiveData<Boolean> by lazy {
        MutableLiveData(false)
    }
}