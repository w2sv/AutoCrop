package com.autocrop.activities.examination.fragments.viewpager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.uielements.recyclerview.BidirectionalRecyclerViewAdapter
import com.autocrop.utils.BlankFun
import com.autocrop.utils.delegates.Consumable

class ViewPagerViewModel : ViewModel(){
    var displayedEntrySnackbar = false

    val dataSet = BidirectionalViewPagerDataSet(ExaminationActivityViewModel.cropBundles)

    //$$$$$$$$$$$$$$$
    // View Removal $
    //$$$$$$$$$$$$$$$

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

    //$$$$$$$$
    // Views $
    //$$$$$$$$

    fun initialViewPosition(): Int =
        (BidirectionalRecyclerViewAdapter.N_VIEWS / 2).let { halvedMaxViews ->
            halvedMaxViews - dataSet.correspondingPosition(halvedMaxViews) + dataSet.currentPosition.value!!
        }
}