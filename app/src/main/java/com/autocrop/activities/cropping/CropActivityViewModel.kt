package com.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.dataclasses.CropBundle
import com.autocrop.utils.android.livedata.IncrementableIntLiveData

class CropActivityViewModel(val uris: ArrayList<Uri>): ViewModel(){
    val nSelectedImages: Int = uris.size

    val nDismissedImages: Int get() = nSelectedImages - cropBundles.size

    val cropBundles: MutableList<CropBundle> = mutableListOf()
    val liveImageNumber = IncrementableIntLiveData(0)
}