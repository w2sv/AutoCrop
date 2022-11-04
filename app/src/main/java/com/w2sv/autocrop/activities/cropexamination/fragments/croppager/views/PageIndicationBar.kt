package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.BaseInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatSeekBar
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.ifNotInEditMode
import com.w2sv.autocrop.utils.android.extensions.show
import com.w2sv.autocrop.utils.android.extensions.viewModel
import kotlin.math.roundToInt

class PageIndicationBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr) {

    private val viewModel by viewModel<CropPagerViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
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