package com.autocrop.activities.iodetermination.fragments.manualcrop

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.CropEdges

class ManualCropViewModelFactory(private val bitmap: Bitmap,
                                 private val initialCropEdges: CropEdges,
                                 private val cropEdgePairCandidates: List<CropEdges>): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ManualCropViewModel(
            bitmap,
            initialCropEdges,
            cropEdgePairCandidates
        ) as T
}