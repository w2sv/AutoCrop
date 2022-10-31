package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet

class CropPagerViewModel : ViewModel() {
    val dataSet = BidirectionalViewPagerDataSet(IODeterminationActivityViewModel.cropBundles)

    //$$$$$$$$$$$$$
    // AutoScroll $
    //$$$$$$$$$$$$$

    var scroller: Scroller? = null

    val liveAutoScroll: LiveData<Boolean> by lazy {
        MutableLiveData(
            BooleanPreferences.autoScroll && dataSet.size > 1
        )
    }

    val autoScrolls: Int
        get() = dataSet.size - dataSet.livePosition.value!!
}