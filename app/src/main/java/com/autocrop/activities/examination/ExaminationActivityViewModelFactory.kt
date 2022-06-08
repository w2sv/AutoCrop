package com.autocrop.activities.examination

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExaminationActivityViewModelFactory(private val validSaveDirDocumentUri: Uri?, private val nDismissedScreenshots: Int?)
        : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ExaminationActivityViewModel(
        validSaveDirDocumentUri,
        nDismissedScreenshots
    ) as T
}