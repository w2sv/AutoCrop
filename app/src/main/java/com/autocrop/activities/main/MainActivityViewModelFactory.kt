package com.autocrop.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.dataclasses.IOSynopsis

class MainActivityViewModelFactory(private val IOSynopsis: IOSynopsis?):
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainActivityViewModel(IOSynopsis) as T
}