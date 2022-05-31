package com.autocrop.activities.examination.fragments.comparison

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ComparisonViewModel: ViewModel() {
    val displayingScreenshot: LiveData<Boolean> by lazy {
        MutableLiveData()
    }
}