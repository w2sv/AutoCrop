package com.autocrop.activities.examination.fragments.viewpager

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

class PageChangeHandler(private val viewModel: ViewPagerViewModel)
        : ViewPager2.OnPageChangeCallback(){

    /**
     * [viewModel].setDataSetPosition if not blocked
     */
    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        viewModel.dataSet.currentPosition.update(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)

        if (state == ViewPager.SCROLL_STATE_IDLE)
            viewModel.onScrollStateIdleListenerConsumable?.invoke()
    }
}