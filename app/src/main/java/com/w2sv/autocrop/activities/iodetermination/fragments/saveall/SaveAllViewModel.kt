package com.w2sv.autocrop.activities.iodetermination.fragments.saveall

import androidx.lifecycle.ViewModel
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.w2sv.autocrop.utils.android.livedata.IncrementableIntLiveData

class SaveAllViewModel : ViewModel() {
    val nImagesToBeSaved = IODeterminationActivityViewModel.cropBundles.size
    val liveCropNumber = IncrementableIntLiveData(0)
}