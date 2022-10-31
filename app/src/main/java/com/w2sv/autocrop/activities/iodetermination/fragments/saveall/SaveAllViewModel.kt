package com.w2sv.autocrop.activities.iodetermination.fragments.saveall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivityViewModel

class SaveAllViewModel : ViewModel() {
    val nImagesToBeSaved = IODeterminationActivityViewModel.cropBundles.size
    val liveCropNumber: LiveData<Int> by lazy {
        MutableLiveData(1)
    }
}