package com.autocrop.activities.examination.fragments.viewpager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.Toolbar
import androidx.core.text.bold
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.uielements.view.*
import com.w2sv.autocrop.R

private class ViewPagerViewModelRetriever(context: Context):
    ViewModelRetrievingView<ViewPagerFragmentViewModel, ExaminationActivity>(
        context,
        ViewPagerFragmentViewModel::class.java
    )

class ExaminationToolBar(context: Context, attr: AttributeSet)
    : Toolbar(context, attr){

    fun dropDownAnimation(){
        show()

        startAnimation(AnimationUtils.loadAnimation(context, R.anim.animate_slide_down_enter))
    }
}

class PageIndicationElements(context: Context, attr: AttributeSet)
    : RelativeLayout(context, attr){

    fun shrinkHidingAnimation(){
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
}

abstract class PageDependentTextView(context: Context, attr: AttributeSet, stringId: Int):
    ExtendedTextView(context, attr, stringId),
    ViewModelRetriever<ViewPagerFragmentViewModel> by ViewPagerViewModelRetriever(context) {

    abstract fun updateText(position: Int)
}

class DiscardedTextView(context: Context, attr: AttributeSet):
    PageDependentTextView(context, attr, R.string.discarded) {

    override fun updateText(position: Int) {
        with(viewModel.dataSet[position]) {
            text = SpannableStringBuilder()
                .append(getString())
                .bold { append(" ${discardedPercentage}%") }
                .append("=")
                .bold { append("${approximateDiscardedFileSize}kb") }
        }
    }
}

class PageIndicationTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr, R.string.fracture) {
    override fun updateText(position: Int){ text = getString().format(position + 1, viewModel.dataSet.size) }
}