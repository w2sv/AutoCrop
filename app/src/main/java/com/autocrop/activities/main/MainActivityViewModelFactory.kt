package com.autocrop.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.iodetermination.IODeterminationActivity

class MainActivityViewModelFactory(private val ioResults: IODeterminationActivity.Results?)
    : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainActivityViewModel(ioResults) as T
}