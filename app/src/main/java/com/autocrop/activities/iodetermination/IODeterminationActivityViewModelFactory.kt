package com.autocrop.activities.iodetermination

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IODeterminationActivityViewModelFactory(private val validSaveDirDocumentUri: Uri?, private val nDismissedScreenshots: Int)
        : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = IODeterminationActivityViewModel(
        validSaveDirDocumentUri,
        nDismissedScreenshots
    ) as T
}