package com.autocrop.activities.iodetermination.fragments.croppager.pager

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel

class PageChangeHandler(private val viewModel: CropPagerViewModel) : ViewPager2.OnPageChangeCallback() {

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