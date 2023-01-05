package com.w2sv.autocrop.activities.examination.fragments.croppager.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.BaseInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatSeekBar
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.examination.fragments.croppager.CropPagerFragment
import kotlin.math.roundToInt

class PageIndicationBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr) {

    private val viewModel by viewModel<CropPagerFragment.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            isEnabled = false  // disables manual dragging

            if (viewModel.dataSet.size != 1)
                show()
        }
    }

    fun update(dataSetPosition: Int) {
        progressAnimation?.cancel()

        progress(dataSetPosition)?.let { newProgress ->
            progressAnimation = ObjectAnimator.ofInt(this, "progress", newProgress).apply {
                with(animationInterpolatorWithDuration(newProgress)) {
                    interpolator = first
                    duration = second
                }
                start()
            }
        }
    }

    private var progressAnimation: ObjectAnimator? = null

    private fun progress(pageIndex: Int): Int? =
        if (viewModel.dataSet.size == 1)
            null
        else
            (max.toFloat() / (viewModel.dataSet.lastIndex).toFloat() * pageIndex).roundToInt()

    private fun animationInterpolatorWithDuration(newProgress: Int): Pair<BaseInterpolator, Long> =
        if (setOf(0, 100) == setOf(progress, newProgress))
            BounceInterpolator() to resources.getLong(R.integer.delay_medium)
        else
            DecelerateInterpolator() to resources.getLong(R.integer.delay_small)
}