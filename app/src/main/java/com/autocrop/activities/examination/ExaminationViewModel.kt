package com.autocrop.activities.examination

import androidx.lifecycle.ViewModel
import com.autocrop.types.CropBundle

class ExaminationViewModel(val nDismissedImages: Int): ViewModel() {

    companion object{
        lateinit var cropBundles: MutableList<CropBundle>
    }

    private var _nSavedCrops = 0
    val nSavedCrops: Int
        get() = _nSavedCrops

    private var _nDeletedScreenshots = 0
    val nDeletedScreenshots: Int
        get() = _nDeletedScreenshots

    fun incrementImageFileIOCounters(deletedScreenshot: Boolean){
        _nSavedCrops++
        if (deletedScreenshot)
            _nDeletedScreenshots++
    }
}