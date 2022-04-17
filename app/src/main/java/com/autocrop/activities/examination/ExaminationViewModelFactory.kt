package com.autocrop.activities.examination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExaminationViewModelFactory(private val documentUriWritePermissionValid: Boolean?)
        : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ExaminationActivityViewModel(
        documentUriWritePermissionValid
    ) as T
}