package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyrebirdstudio.croppylib.CropRequest

class ImageCropViewModel(val bitmap: Bitmap, val cropRequest: CropRequest)
    : ViewModel() {

    val cropRect: LiveData<Rect> by lazy {
        MutableLiveData(cropRequest.initialCropRect)
    }
}