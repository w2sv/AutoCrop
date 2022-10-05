package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.fragment.cropview.CropEdges

class CropViewModelFactory(private val bitmap: Bitmap,
                           private val initialCropEdges: Pair<Int, Int>,
                           private val cropEdgePairCandidates: List<CropEdges<Int>>,
                           private val croppyTheme: CroppyTheme): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CropViewModel(
            bitmap,
            initialCropEdges.run {
                CropEdges(first.toFloat(), second.toFloat())
            },
            cropEdgePairCandidates,
            croppyTheme
        ) as T
}