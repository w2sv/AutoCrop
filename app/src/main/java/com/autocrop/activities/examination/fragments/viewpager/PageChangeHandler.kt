package com.autocrop.activities.examination.fragments.viewpager

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

class PageChangeHandler(
    viewPagerStartPosition: Int,
    private val updatePageDependentViews: (position: Int, Boolean) -> Unit)
        : ViewPager2.OnPageChangeCallback(){

    private var previousPosition = viewPagerStartPosition
    var updateViews = true

    /**
     * [updatePageDependentViews] if not blocked
     */
    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        if (updateViews)
            updatePageDependentViews(position, position > previousPosition)
        previousPosition = position
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