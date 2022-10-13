package com.autocrop.activities.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.iodetermination.IOSynopsis

class MainActivityViewModelFactory(private val ioSynopsis: IOSynopsis?,
                                   private val savedCropUris: ArrayList<Uri>?)
    : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainActivityViewModel(ioSynopsis, savedCropUris) as T
}