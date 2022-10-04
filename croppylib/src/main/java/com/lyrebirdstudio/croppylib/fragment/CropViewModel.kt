package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyrebirdstudio.croppylib.CropRequest
import com.lyrebirdstudio.croppylib.CroppyTheme

class CropViewModel(val bitmap: Bitmap,
                    val initialCropRect: RectF,
                    val cropEdgePairCandidates: List<Pair<Int, Int>>,
                    val croppyTheme: CroppyTheme): ViewModel() {

    val cropRectF: LiveData<RectF> by lazy {
        MutableLiveData(initialCropRect)
    }
}