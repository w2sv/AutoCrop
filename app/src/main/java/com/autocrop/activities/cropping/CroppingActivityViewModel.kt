package com.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.collections.CropBundle
import com.autocrop.utilsandroid.asMutable

class CroppingActivityViewModel(val uris: ArrayList<Uri>): ViewModel(){

    val cropBundles: MutableList<CropBundle> = mutableListOf()

    val nSelectedImages: Int = uris.size
    val nDismissedImages: Int
        get() = nSelectedImages - cropBundles.size

    val currentImageNumber: LiveData<Int> by lazy {
        MutableLiveData(0)
    }
    fun incrementCurrentImageNumber(){
        currentImageNumber.asMutable.value = currentImageNumber.value?.plus(1)
    }
}