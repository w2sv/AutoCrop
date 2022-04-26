package com.autocrop.activities.examination.fragments.viewpager.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatSeekBar
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragmentViewModel
import com.autocrop.uielements.view.ViewModelRetriever
import com.w2sv.autocrop.R

class PageIndicationSeekBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr),
    ViewModelRetriever<ViewPagerFragmentViewModel> by ViewPagerViewModelRetriever(context) {

    /**
     * Disable manual dragging
     */
    init {
        isEnabled = false
    }

    fun update(dataSetPosition: Int, scrolledRight: Boolean) {
        val animationDuration = mapOf(
            BounceInterpolator::class.java to 400L,
            DecelerateInterpolator::class.java to 100L
        )

        val newProgress: Int = viewModel.pageIndicationSeekbarPagePercentage(dataSetPosition, max)

        with(ObjectAnimator.ofInt(this,"progress", newProgress)) {
            with(if (displayBouncingAnimation(scrolledRight, newProgress)) BounceInterpolator::class.java else DecelerateInterpolator::class.java){
                interpolator = newInstance()
                duration = animationDuration.getValue(this)
            }

            start()
        }
    }

    private fun displayBouncingAnimation(scrolledRight: Boolean, newProgress: Int): Boolean =
        listOf(progress, newProgress).let {
            (it == listOf(0, 100) && !scrolledRight) || (it == listOf(100, 0) && scrolledRight)
        }
}

class PageIndicationTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr, R.string.fracture) {
    override fun updateText(position: Int){
        text = stringResource.format(position + 1, viewModel.dataSet.size)
    }
}