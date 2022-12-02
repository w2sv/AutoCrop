package com.w2sv.autocrop.ui

import android.view.View
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

class CubeOutPageTransformer: ViewPager2.PageTransformer{
    override fun transformPage(page: View, position: Float) {
        with(page) {
            pivotX = (if (position < 0) width else 0).toFloat()
            pivotY = height * 0.5f
            rotationY = 90f * position
        }
    }
}