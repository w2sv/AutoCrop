package com.lyrebirdstudio.croppylib.fragment

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lyrebirdstudio.croppylib.CropRequest
import com.lyrebirdstudio.croppylib.utils.bitmap.resizedBitmap

class ImageCropViewModel(private val app: Application, val cropRequest: CropRequest)
    : AndroidViewModel(app) {

    val cropHeight: LiveData<Int> by lazy{
        MutableLiveData(cropRequest.initialCropRect.height())
    }
    val resizedBitmap: Bitmap = resizedBitmap(cropRequest.sourceUri, app.applicationContext)
}