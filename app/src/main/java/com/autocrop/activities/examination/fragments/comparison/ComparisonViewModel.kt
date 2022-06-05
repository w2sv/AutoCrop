package com.autocrop.activities.examination.fragments.comparison

import androidx.lifecycle.*
import com.autocrop.collections.CropBundle

class ComparisonViewModelFactory(private val cropBundle: CropBundle)
    : ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ComparisonViewModel(cropBundle) as T
}

class ComparisonViewModel(val cropBundle: CropBundle): ViewModel() {
    companion object{
        var displayInstructionSnackbar = true
    }
}