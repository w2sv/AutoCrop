package com.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.collections.CropBundle
import com.autocrop.utilsandroid.IncrementableIntLiveData

class CroppingActivityViewModel(val uris: ArrayList<Uri>): ViewModel(){
    val nSelectedImages: Int = uris.size

    val nDismissedImages: Int get() = nSelectedImages - cropBundles.size

    val cropBundles: MutableList<CropBundle> = mutableListOf()
    val currentImageNumber = IncrementableIntLiveData(0)
}