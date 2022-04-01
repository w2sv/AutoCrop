package com.autocrop.activities.examination

import androidx.lifecycle.ViewModel


class ExaminationViewModel(val nDismissedImages: Int): ViewModel() {
    private var _nSavedCrops = 0
    val nSavedCrops: Int
        get() = _nSavedCrops

    private var _nDeletedScreenshots = 0
    val nDeletedScreenshots: Int
        get() = _nDeletedScreenshots

    fun incrementImageFileIOCounters(by: Int, deletedScreenshots: Boolean){
        _nSavedCrops += by
        if (deletedScreenshots)
            _nDeletedScreenshots += by
    }
}