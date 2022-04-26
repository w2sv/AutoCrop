package com.autocrop.activities.examination.fragments.viewpager

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

class PageChangeHandler(private val viewModel: ViewPagerViewModel)
        : ViewPager2.OnPageChangeCallback(){

    private var previousPosition = viewModel.initialViewPosition
    var blockViewUpdatingOnNextPageChange = false

    /**
     * [viewModel].setDataSetPosition if not blocked
     */
    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        if (!blockViewUpdatingOnNextPageChange){
            viewModel.setDataSetPosition(position, position > previousPosition)
            previousPosition = position
        }
        else
            blockViewUpdatingOnNextPageChange = false
    }

    var onNextScrollCompletion: (() -> Unit)? = null
    var removeView: (() -> Unit)? = null

    override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)

        if (state == ViewPager.SCROLL_STATE_IDLE){
            onNextScrollCompletion?.let {
                it()
                onNextScrollCompletion = null
            }
            removeView?.let {
                it()
                removeView = null
            }
        }
    }
}