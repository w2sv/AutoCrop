package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyrebirdstudio.croppylib.CropRequest

class ImageCropViewModel(val bitmap: Bitmap, val cropRequest: CropRequest)
    : ViewModel() {

    val cropRectF: LiveData<RectF> by lazy {
        MutableLiveData(cropRequest.initialCropRect)
    }
}