package com.autocrop.activities.examination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExaminationViewModelFactory(private val conductAutoScroll: Boolean,
                                  private val longAutoScrollDelay: Boolean)
        : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ExaminationViewModel(
        conductAutoScroll,
        longAutoScrollDelay
    ) as T
}