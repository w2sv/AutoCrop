package com.w2sv.autocrop.ui.views

import android.view.View
import androidx.recyclerview.widget.RecyclerView
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
        (0 until maxScrolls).forEach {
            setCurrentItem(currentItem + 1, true)
            if (it != maxScrolls - 1)
                delay(period)
        }
        onFinishedListener()
    }

@Suppress("UNCHECKED_CAST")
fun <VH : RecyclerView.ViewHolder> ViewPager2.currentViewHolder(): VH? =
    recyclerView.findViewHolderForAdapterPosition(currentItem) as? VH

val ViewPager2.recyclerView: RecyclerView
    get() = getChildAt(0) as RecyclerView

class CubeOutPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        with(page) {
            pivotX = (if (position < 0) width else 0).toFloat()
            pivotY = height * 0.5f
            rotationY = 90f * position
        }
    }
}