package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyrebirdstudio.croppylib.CroppyTheme

class CropViewModelFactory(private val bitmap: Bitmap,
                           private val initialCropRect: RectF,
                           private val cropEdgePairCandidates: List<Pair<Int, Int>>,
                           private val croppyTheme: CroppyTheme): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CropViewModel(bitmap, initialCropRect, cropEdgePairCandidates, croppyTheme) as T
}