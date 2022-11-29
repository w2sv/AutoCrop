package com.w2sv.autocrop.ui

import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun ViewPager2.scrollPeriodically(
    coroutineScope: CoroutineScope,
    maxScrolls: Int,
    period: Long,
    onFinishedListener: () -> Unit
): Job =
    coroutineScope.launch(Dispatchers.Main) {
        val finalIterationIndex = maxScrolls - 1
        (0 until maxScrolls).forEach {
            setCurrentItem(currentItem + 1, true)
            if (it != finalIterationIndex)
                delay(period)
        }
        onFinishedListener()
    }