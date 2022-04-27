package com.autocrop.activities.examination.fragments.viewpager

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.utils.BlankFun

class PageChangeHandler(private val viewModel: ViewPagerViewModel)
        : ViewPager2.OnPageChangeCallback(){

    private var previousPosition = viewModel.initialViewPosition
    var blockViewUpdatingOnNextPageChange = false

    /**
     * [viewModel].setDataSetPosition if not blocked
     */
    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        if (blockViewUpdatingOnNextPageChange)
            blockViewUpdatingOnNextPageChange = false
        else {
            viewModel.setDataSetPosition(position, position > previousPosition)
            previousPosition = position
        }
    }

    private var onNextScrollCompletion: BlankFun? = null
    fun addToOnNextScrollCompletion(function: BlankFun){
        onNextScrollCompletion = onNextScrollCompletion?.let {
            {
                it()
                function()
            }
        } ?: function
    }

    override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)

        if (state == ViewPager.SCROLL_STATE_IDLE && onNextScrollCompletion != null){
            onNextScrollCompletion!!()
            onNextScrollCompletion = null
        }
    }
}