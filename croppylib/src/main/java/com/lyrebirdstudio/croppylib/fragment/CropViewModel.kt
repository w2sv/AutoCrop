package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.fragment.cropview.CropEdges

class CropViewModel(val bitmap: Bitmap,
                    val initialCropEdges: CropEdges,
                    val cropEdgePairCandidates: List<CropEdges>,
                    val croppyTheme: CroppyTheme): ViewModel() {

    val cropEdges: LiveData<CropEdges> by lazy {
        MutableLiveData(initialCropEdges)
    }
}