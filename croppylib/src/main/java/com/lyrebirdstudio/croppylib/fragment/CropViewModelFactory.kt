package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.fragment.cropview.CropEdges

class CropViewModelFactory(private val bitmap: Bitmap,
                           private val initialCropEdges: CropEdges,
                           private val cropEdgePairCandidates: List<CropEdges>,
                           private val croppyTheme: CroppyTheme): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CropViewModel(
            bitmap,
            initialCropEdges,
            cropEdgePairCandidates,
            croppyTheme
        ) as T
}