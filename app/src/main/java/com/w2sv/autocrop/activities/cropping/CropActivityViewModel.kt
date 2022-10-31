package com.w2sv.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.w2sv.autocrop.CropBundle

class CropActivityViewModel(val uris: ArrayList<Uri>) : ViewModel() {

    class Factory(private val uris: ArrayList<Uri>) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CropActivityViewModel(
                uris
            ) as T
    }

    val nSelectedImages: Int = uris.size
    val nDismissedImages: Int get() = nSelectedImages - cropBundles.size

    val cropBundles: MutableList<CropBundle> = mutableListOf()
    val liveImageNumber: LiveData<Int> by lazy {
        MutableLiveData(0)
    }
}