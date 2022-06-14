package com.autocrop.activities.examination.fragments.viewpager

import androidx.viewpager2.widget.ViewPager2
import com.autocrop.uielements.recyclerview.ExtendedRecyclerViewAdapter

class CropViewPagerProxy(private val viewPager2: ViewPager2, private val viewModel: ViewPagerViewModel){
    init {
        viewPager2.adapter = CropPagerAdapter(viewModel)

        viewPager2.registerOnPageChangeCallback(
            PageChangeHandler(
                viewModel
            )
        )
    }

    fun setInitialView(){
        viewPager2.setCurrentItem(
            viewModel.initialViewPosition(),
            false
        )
    }

    /**
     * • scroll to subsequent position
     * • remove cropBundle from dataSet
     * • rotate dataSet such that it will subsequently align with the determined newViewPosition again
     * • reset preloaded views around newViewPosition
     * • update pageIndex dependent views
     */
    fun removeView(dataSetPosition: Int) {
        val subsequentViewPosition = viewModel.dataSet.subsequentViewPosition(viewPager2.currentItem, dataSetPosition)

        // scroll to newViewPosition with blocked pageDependentViewUpdating
        viewModel.dataSet.currentPosition.blockSubsequentUpdate()
        viewPager2.setCurrentItem(subsequentViewPosition, true)

        viewModel.onScrollStateIdleListenerConsumable = {
            // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
            // aligning with newViewPosition
            viewModel.dataSet.removeAtAndRealign(dataSetPosition, subsequentViewPosition)

            // reset surrounding views
            (viewPager2.adapter as ExtendedRecyclerViewAdapter).resetCachedViewsAround(subsequentViewPosition)

            // update currentPosition
            viewModel.dataSet.currentPosition.update(subsequentViewPosition)
        }
    }
}