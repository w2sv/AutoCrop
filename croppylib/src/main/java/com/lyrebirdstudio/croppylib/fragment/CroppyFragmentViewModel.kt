package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyrebirdstudio.croppylib.CropEdges
import com.lyrebirdstudio.croppylib.CroppyTheme

class CroppyFragmentViewModel(val bitmap: Bitmap,
                              val initialCropEdges: CropEdges,
                              val cropEdgePairCandidates: List<CropEdges>,
                              val croppyTheme: CroppyTheme): ViewModel() {

    val cropEdges: LiveData<CropEdges> by lazy {
        MutableLiveData(initialCropEdges)
    }
}