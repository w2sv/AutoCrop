package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.fragment.cropview.CropEdges

class CropViewModel(val bitmap: Bitmap,
                    val initialCropRect: RectF,
                    val cropEdgePairCandidates: List<CropEdges>,
                    val croppyTheme: CroppyTheme): ViewModel() {

    val cropRectF: LiveData<RectF> by lazy {
        MutableLiveData(initialCropRect)
    }
}