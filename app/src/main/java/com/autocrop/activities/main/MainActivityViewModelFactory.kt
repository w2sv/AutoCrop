package com.autocrop.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.collections.ImageFileIOSynopsis

class MainActivityViewModelFactory(private val imageFileIOSynopsis: ImageFileIOSynopsis?):
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainActivityViewModel(imageFileIOSynopsis) as T
}