package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.fragment.cropview.CropEdges

class CropViewModel(val bitmap: Bitmap,
                    val initialCropEdges: CropEdges<Float>,
                    val cropEdgePairCandidates: List<CropEdges<Int>>,
                    val croppyTheme: CroppyTheme): ViewModel() {

    val cropEdgesF: LiveData<CropEdges<Float>> by lazy {
        MutableLiveData(initialCropEdges)
    }
}