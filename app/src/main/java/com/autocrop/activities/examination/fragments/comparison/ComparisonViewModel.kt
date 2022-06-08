package com.autocrop.activities.examination.fragments.comparison

import androidx.lifecycle.ViewModel
import com.autocrop.collections.CropBundle
import kotlin.properties.Delegates

class ComparisonViewModel(val cropBundle: CropBundle): ViewModel(){
    var enterTransitionCompleted = false

    var displayScreenshot by Delegates.notNull<Boolean>()
}