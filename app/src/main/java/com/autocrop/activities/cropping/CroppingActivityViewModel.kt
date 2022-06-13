package com.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.autocrop.collections.CropBundle

class CroppingActivityViewModel(val uris: ArrayList<Uri>): ViewModel(){

    val cropBundles: MutableList<CropBundle> = mutableListOf()

    val nSelectedImages: Int = uris.size
    val nDismissedImages: Int
        get() = nSelectedImages - cropBundles.size

    val currentImageNumber: CurrentImageNumber = CurrentImageNumber()

    class CurrentImageNumber: LiveData<Int>(0){
        fun increment(){
            postValue(value?.plus(1))
        }
    }
}