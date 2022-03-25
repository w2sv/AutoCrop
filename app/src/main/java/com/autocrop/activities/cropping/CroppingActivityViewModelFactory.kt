package com.autocrop.activities.cropping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CroppingActivityViewModelFactory(
    private val nSelectedImages: Int,
    private val progressBarMax: Int)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CroppingActivityViewModel(
        nSelectedImages,
        progressBarMax
    ) as T
}