package com.autocrop.activities.examination.fragments.viewpager

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.utils.BlankFun

class PageChangeHandler(private val viewModel: ViewPagerViewModel)
        : ViewPager2.OnPageChangeCallback(){

    private var previousPosition = viewModel.initialViewPosition

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
            viewModel.consumeScrollStateIdleListenerIfSet()
    }
}