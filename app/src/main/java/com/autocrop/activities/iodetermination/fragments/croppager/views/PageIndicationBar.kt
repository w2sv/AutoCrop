package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.BaseInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatSeekBar
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.ui.elements.view.activityViewModelLazy
import com.autocrop.ui.elements.view.ifNotInEditMode
import com.autocrop.ui.elements.view.show
import kotlin.math.roundToInt

class PageIndicationBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr) {

    private val viewModel by activityViewModelLazy<CropPagerViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            isEnabled = false  // disable manual dragging

            if (viewModel.dataSet.size != 1)
                show()
        }
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
        if (viewModel.dataSet.size == 1)
            null
        else
            (max.toFloat() / (viewModel.dataSet.lastIndex).toFloat() * pageIndex).roundToInt()

    private fun getInterpolator(newProgress: Int, bouncingAnimationBlocked: Boolean): BaseInterpolator =
        if (!bouncingAnimationBlocked && setOf(0, 100) == setOf(progress, newProgress))
            BounceInterpolator()
        else
            DecelerateInterpolator()
}