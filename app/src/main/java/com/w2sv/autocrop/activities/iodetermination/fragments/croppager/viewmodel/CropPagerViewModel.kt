package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.android.BackPressListener
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet

class CropPagerViewModel : ViewModel() {
    val dataSet = BidirectionalViewPagerDataSet(IODeterminationActivityViewModel.cropBundles)

    val backPressHandler = BackPressListener(viewModelScope)

    //$$$$$$$$$$$$$
    // AutoScroll $
    //$$$$$$$$$$$$$

    var scroller: Scroller? = null

    val liveAutoScroll: LiveData<Boolean> = MutableLiveData(BooleanPreferences.autoScroll && dataSet.size > 1)

    val autoScrolls: Int
        get() = dataSet.size - dataSet.livePosition.value!!
}