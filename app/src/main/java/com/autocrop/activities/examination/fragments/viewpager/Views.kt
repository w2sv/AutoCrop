package com.autocrop.activities.examination.fragments.viewpager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.text.bold
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.utils.android.*
import com.w2sv.autocrop.R

private class ViewPagerViewModelRetriever(context: Context):
    AbstractViewModelRetriever<ViewPagerFragmentViewModel, ExaminationActivity>(context, ViewPagerFragmentViewModel::class.java)

class PageIndicationSeekBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr),
    ViewModelRetriever<ViewPagerFragmentViewModel> by ViewPagerViewModelRetriever(context) {

    init {
        if (viewModel.dataSet.size > 1){
            show()
            progress = viewModel.pageIndicationSeekbarPagePercentage(0, max)
            isEnabled = false  // disables dragging of bar
        }
    }

    fun update(dataSetPosition: Int, scrolledRight: Boolean) {
        val targetProgress: Int = viewModel.pageIndicationSeekbarPagePercentage(dataSetPosition, max)
        val displayBouncingAnimation: Boolean = listOf(progress, targetProgress).let {
            (it == listOf(0, 100) && !scrolledRight) || (it == listOf(100, 0) && scrolledRight)
        }

        with(ObjectAnimator.ofInt(this,"progress", targetProgress)) {
             val (_duration, _interpolator) = if (displayBouncingAnimation) 400L to BounceInterpolator() else 100L to DecelerateInterpolator()
            interpolator = _interpolator
            duration = _duration
            start()
        }
    }

    fun hideAnimated(){
        animate()
            .scaleX(0f)
            .scaleY(0f)
            .setInterpolator(AnticipateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    remove()
                }
            })
            .duration = 700L
    }
}

abstract class PageDependentTextView(context: Context, attr: AttributeSet, stringId: Int):
    ExtendedTextView(context, attr, stringId),
    ViewModelRetriever<ViewPagerFragmentViewModel> by ViewPagerViewModelRetriever(context) {

    init{
        updateText(0)
    }

    abstract fun updateText(viewPagerDataSetPosition: Int)
    protected fun getString(viewPagerDataSetPosition: Int): String = super.getString().format(*formatArgs(viewPagerDataSetPosition).toTypedArray())
    protected abstract fun formatArgs(viewPagerDataSetPosition: Int): List<Int>
}

class DiscardedTextView(context: Context, attr: AttributeSet):
    PageDependentTextView(context, attr, R.string.discarded) {

    override fun updateText(viewPagerDataSetPosition: Int) {
        with(viewModel.dataSet[viewPagerDataSetPosition]) {
            text = SpannableStringBuilder()
                .append(getString(0))
                .bold { append(" ${discardedPercentage}%") }
                .append("=")
                .bold { append("${approximateDiscardedFileSize}kb") }
        }
    }
    override fun formatArgs(viewPagerDataSetPosition: Int): List<Int> = listOf()
}

class PageIndicationTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr, R.string.fracture) {
    override fun updateText(pageIndex: Int){ text = getString(pageIndex) }
    override fun formatArgs(pageIndex: Int): List<Int> = listOf(pageIndex + 1, viewModel.dataSet.size)
}