package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.fragment.cropview.CropEdges

class CropViewModelFactory(private val bitmap: Bitmap,
                           private val initialCropRect: RectF,
                           private val cropEdgePairCandidates: List<CropEdges>,
                           private val croppyTheme: CroppyTheme): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CropViewModel(bitmap, initialCropRect, cropEdgePairCandidates, croppyTheme) as T
}