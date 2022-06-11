package com.lyrebirdstudio.croppylib.fragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyrebirdstudio.croppylib.CropRequest

class ImageCropViewModelFactory(private val app: Application, private val cropRequest: CropRequest)
    : ViewModelProvider.AndroidViewModelFactory(app){

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ImageCropViewModel(app, cropRequest) as T
}