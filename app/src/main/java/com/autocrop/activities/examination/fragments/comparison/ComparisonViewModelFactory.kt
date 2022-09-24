package com.autocrop.activities.examination.fragments.comparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.dataclasses.CropBundle

class ComparisonViewModelFactory(private val cropBundle: CropBundle)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ComparisonViewModel(cropBundle) as T
}