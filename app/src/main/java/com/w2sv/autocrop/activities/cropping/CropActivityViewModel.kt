package com.w2sv.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.w2sv.autocrop.CropBundle
import com.w2sv.bidirectionalviewpager.livedata.MutableListLiveData

class CropActivityViewModel(private val uris: ArrayList<Uri>) : ViewModel() {

    class Factory(private val uris: ArrayList<Uri>) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CropActivityViewModel(
                uris
            ) as T
    }

    val nImages: Int get() = uris.size
    val nUncroppedImages: Int get() = nImages - liveCropBundles.size

    val imminentUris: List<Uri>
        get() = uris.run {
            subList(liveCropBundles.size, size)
        }

    val liveCropBundles = MutableListLiveData<CropBundle>(mutableListOf())
}