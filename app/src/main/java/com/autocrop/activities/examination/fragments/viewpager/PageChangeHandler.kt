package com.autocrop.activities.examination.fragments.viewpager

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

class PageChangeHandler(private val viewModel: ViewPagerViewModel)
        : ViewPager2.OnPageChangeCallback(){

    private var previousPosition = -1

    /**
     * [viewModel].setDataSetPosition if not blocked
     */
    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        viewModel.setDataSetPosition(position, position > previousPosition)
        previousPosition = position
    }

    override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)

        if (state == ViewPager.SCROLL_STATE_IDLE)
            viewModel.scrollStateIdleListenerConsumable.consume()?.let {
                it()
            }
    }
}