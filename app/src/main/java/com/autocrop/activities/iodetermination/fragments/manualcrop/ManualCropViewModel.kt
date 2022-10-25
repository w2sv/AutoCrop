package com.autocrop.activities.iodetermination.fragments.manualcrop

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.CropEdges

class ManualCropViewModel(val bitmap: Bitmap,
                          val initialCropEdges: CropEdges,
                          val cropEdgePairCandidates: List<CropEdges>): ViewModel() {

    val cropEdges: LiveData<CropEdges> by lazy {
        MutableLiveData(initialCropEdges)
    }
}