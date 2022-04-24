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
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.text.bold
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.uielements.view.*
import com.w2sv.autocrop.R

private class ViewPagerViewModelRetriever(context: Context):
    ViewModelRetrievingView<ViewPagerFragmentViewModel, ExaminationActivity>(
        context,
        ViewPagerFragmentViewModel::class.java
    )

class PageIndicationElements(context: Context, attr: AttributeSet)
    : RelativeLayout(context, attr){

    fun shrinkAndRemove(){
        animate()
            .scaleX(0f)
            .scaleY(0f)
            .setInterpolator(AnticipateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    remove()
                }
            })
            .duration = resources.getInteger(R.integer.visibility_changing_animation_duration).toLong()
    }
}

class PageIndicationSeekBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr),
    ViewModelRetriever<ViewPagerFragmentViewModel> by ViewPagerViewModelRetriever(context) {

    init {
        isEnabled = false  // disables dragging of bar
    }

    fun update(dataSetPosition: Int, scrolledRight: Boolean) {
        val targetProgress: Int = viewModel.pageIndicationSeekbarPagePercentage(dataSetPosition, max)
        val displayBouncingAnimation: Boolean = listOf(progress, targetProgress).let {
            (it == listOf(0, 100) && !scrolledRight) || (it == listOf(100, 0) && scrolledRight)
        }

        val interpolatorToAnimationDuration = mapOf(
            BounceInterpolator::class.java to 400L,
            DecelerateInterpolator::class.java to 100L
        )

        with(ObjectAnimator.ofInt(this,"progress", targetProgress)) {
            val interpolator = if (displayBouncingAnimation) BounceInterpolator() else DecelerateInterpolator()

            this.interpolator = interpolator
            duration = interpolatorToAnimationDuration.getValue(interpolator::class.java)
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