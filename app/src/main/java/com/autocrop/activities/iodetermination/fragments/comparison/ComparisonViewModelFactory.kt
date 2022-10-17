package com.autocrop.activities.iodetermination.fragments.comparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.CropBundle

class ComparisonViewModelFactory(private val cropBundle: CropBundle)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ComparisonViewModel(cropBundle) as T
}