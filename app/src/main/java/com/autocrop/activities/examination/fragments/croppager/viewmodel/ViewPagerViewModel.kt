package com.autocrop.activities.examination.fragments.croppager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.ui.elements.recyclerview.BidirectionalRecyclerViewAdapter
import com.autocrop.utils.kotlin.BlankFun
import com.autocrop.utils.kotlin.delegates.Consumable

class ViewPagerViewModel : ViewModel(){
    val dataSet = CropBundleDataSet(ExaminationActivityViewModel.cropBundles)

    fun initialViewPosition(): Int =
            (BidirectionalRecyclerViewAdapter.N_VIEWS / 2).let { halvedMaxViews ->
                halvedMaxViews - dataSet.correspondingPosition(halvedMaxViews) + dataSet.currentPosition.value!!
            }

    var onScrollStateIdleListenerConsumable by Consumable<BlankFun>()

    //$$$$$$$$$$$$$
    // AutoScroll $
    //$$$$$$$$$$$$$

    var scroller: Scroller? = null

    val autoScroll: LiveData<Boolean> by lazy {
        MutableLiveData(
                BooleanPreferences.autoScroll && dataSet.size > 1
        )
    }

    val maxAutoScrolls: Int
        get() = dataSet.size - dataSet.currentPosition.value!!
}