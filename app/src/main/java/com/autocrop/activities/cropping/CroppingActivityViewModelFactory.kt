package com.autocrop.activities.cropping

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CroppingActivityViewModelFactory(private val uris: ArrayList<Uri>)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CroppingActivityViewModel(
        uris
    ) as T
}