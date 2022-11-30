package com.w2sv.bidirectionalviewpager.viewpager

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.w2sv.kotlinutils.delegates.Consumable

open class ExtendedOnPageChangeCallback : ViewPager2.OnPageChangeCallback() {

    override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)

        if (state == ViewPager.SCROLL_STATE_IDLE)
            onScrollStateIdle?.invoke()
    }

    fun setRemoveView(f: () -> Unit) {
        onScrollStateIdle = f
    }

    private var onScrollStateIdle by Consumable<() -> Unit>()
}