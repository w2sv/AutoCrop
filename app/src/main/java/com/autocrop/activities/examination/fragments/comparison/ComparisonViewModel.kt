package com.autocrop.activities.examination.fragments.comparison

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class ComparisonViewModel: ViewModel() {
    companion object{
        var displayInstructionSnackbar = true
    }

    val displayScreenshot: LiveData<Boolean> by lazy {
        MutableLiveData()
    }
}