package com.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.types.CropBundle

class CroppingActivityViewModel(val uris: ArrayList<Uri>): ViewModel(){

    val nSelectedImages: Int = uris.size
    val cropBundles: MutableList<CropBundle> = mutableListOf()

    val nDismissedImages: Int
        get() = nSelectedImages - cropBundles.size

    val currentCropNumber: LiveData<Int> by lazy {
        MutableLiveData(0)
    }

    fun incrementCurrentCropNumber(){
        (currentCropNumber as MutableLiveData).value = currentCropNumber.value?.plus(1)
    }
}