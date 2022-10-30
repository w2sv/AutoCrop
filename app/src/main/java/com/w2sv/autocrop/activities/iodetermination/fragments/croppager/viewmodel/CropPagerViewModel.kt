package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.kotlin.VoidFun
import com.w2sv.autocrop.utils.kotlin.delegates.Consumable
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet
import com.w2sv.bidirectionalviewpager.recyclerview.BidirectionalRecyclerViewAdapter

class CropPagerViewModel : ViewModel() {
    val dataSet = BidirectionalViewPagerDataSet(IODeterminationActivityViewModel.cropBundles)

    fun initialViewPosition(): Int =
        (BidirectionalRecyclerViewAdapter.N_VIEWS / 2).let { halvedMaxViews ->
            halvedMaxViews - dataSet.correspondingPosition(halvedMaxViews) + dataSet.currentPosition.value!!
        }

    var onScrollStateIdleListenerConsumable by Consumable<VoidFun>()

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