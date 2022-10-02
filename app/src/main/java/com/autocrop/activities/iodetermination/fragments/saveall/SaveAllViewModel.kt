package com.autocrop.activities.iodetermination.fragments.saveall

import androidx.lifecycle.ViewModel
import com.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.autocrop.utils.android.livedata.IncrementableIntLiveData

class SaveAllViewModel: ViewModel(){
    val nImagesToBeSaved = IODeterminationActivityViewModel.cropBundles.size
    val liveCropNumber = IncrementableIntLiveData(0)
}