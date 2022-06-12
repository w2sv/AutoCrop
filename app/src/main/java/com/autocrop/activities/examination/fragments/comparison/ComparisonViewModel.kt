package com.autocrop.activities.examination.fragments.comparison

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.collections.CropBundle

class ComparisonViewModel(val cropBundle: CropBundle): ViewModel(){
    var enterTransitionCompleted = false

    val displayScreenshot: LiveData<Boolean> by lazy {
        MutableLiveData(false)
    }
}