package com.autocrop.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.iodetermination.IODeterminationActivity

class MainActivityViewModel(val ioResults: IODeterminationActivity.Results?) : ViewModel() {
    class Factory(private val ioResults: IODeterminationActivity.Results?) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainActivityViewModel(ioResults) as T
    }

    var fadeInFlowFieldButtons = true
}