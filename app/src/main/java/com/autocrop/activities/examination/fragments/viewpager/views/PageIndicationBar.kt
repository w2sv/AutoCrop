package com.autocrop.activities.examination.fragments.viewpager.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.BaseInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatSeekBar
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.uielements.view.show
import kotlin.math.roundToInt

class PageIndicationBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context) {

    init {
        isEnabled = false  // disable manual dragging

        if (sharedViewModel.dataSet.size != 1)
            show()
    }

    fun update(dataSetPosition: Int, bouncingAnimationBlocked: Boolean) {
        val animationDuration = mapOf(
            BounceInterpolator::class.java to 400L,
            DecelerateInterpolator::class.java to 100L
        )

        progress(dataSetPosition)?.let { newProgress ->
            with(ObjectAnimator.ofInt(this, "progress", newProgress)) {
                getInterpolator(newProgress, bouncingAnimationBlocked).let{ interpolator ->
                    this.interpolator = interpolator
                    duration = animationDuration.getValue(interpolator.javaClass)
                }
                start()
            }
        }
    }

    private fun progress(pageIndex: Int): Int? =
        if (sharedViewModel.dataSet.size == 1)
            null
        else
            (max.toFloat() / (sharedViewModel.dataSet.lastIndex).toFloat() * pageIndex).roundToInt()

    private fun getInterpolator(newProgress: Int, bouncingAnimationBlocked: Boolean): BaseInterpolator =
        if (!bouncingAnimationBlocked && setOf(0, 100) == setOf(progress, newProgress))
            BounceInterpolator()
        else
            DecelerateInterpolator()
}