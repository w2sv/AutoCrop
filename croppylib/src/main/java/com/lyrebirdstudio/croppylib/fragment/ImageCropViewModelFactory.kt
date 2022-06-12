package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyrebirdstudio.croppylib.CropRequest

class ImageCropViewModelFactory(private val bitmap: Bitmap, private val cropRequest: CropRequest)
    : ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ImageCropViewModel(bitmap, cropRequest) as T
}