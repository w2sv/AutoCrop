package com.autocrop.activities.iodetermination.fragments.manualcrop

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.CropEdges

class ManualCropViewModel(
    val bitmap: Bitmap,
    val initialCropEdges: CropEdges,
    val cropEdgePairCandidates: List<CropEdges>
) : ViewModel() {
    class Factory(
        private val bitmap: Bitmap,
        private val initialCropEdges: CropEdges,
        private val cropEdgePairCandidates: List<CropEdges>
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ManualCropViewModel(
                bitmap,
                initialCropEdges,
                cropEdgePairCandidates
            ) as T
    }

    val cropEdges: LiveData<CropEdges> by lazy {
        MutableLiveData(initialCropEdges)
    }
}