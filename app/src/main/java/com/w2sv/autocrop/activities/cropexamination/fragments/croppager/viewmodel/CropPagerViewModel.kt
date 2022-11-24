package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.BackPressListener
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivityViewModel
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CropPagerViewModel @Inject constructor(booleanPreferences: BooleanPreferences) : ViewModel() {

    val dataSet = BidirectionalViewPagerDataSet(CropExaminationActivityViewModel.cropBundles)

    val backPressHandler = BackPressListener(viewModelScope)

    //$$$$$$$$$$$$$
    // AutoScroll $
    //$$$$$$$$$$$$$

    var scroller: Scroller? = null

    val liveAutoScroll: LiveData<Boolean> = MutableLiveData(booleanPreferences.autoScroll && dataSet.size > 1)

    val autoScrolls: Int
        get() = dataSet.size - dataSet.livePosition.value!!
}