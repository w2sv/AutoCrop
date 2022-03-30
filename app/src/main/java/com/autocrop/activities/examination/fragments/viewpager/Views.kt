package com.autocrop.activities.examination.fragments.viewpager

import android.animation.ObjectAnimator
import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.bold
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.ViewPagerModel
import com.w2sv.autocrop.R

interface ViewModelRetriever{
    val viewModel: ViewPagerModel
}

class ViewModelRetrieverImplementation(context: Context): ViewModelRetriever {
    override val viewModel: ViewPagerModel by lazy {
        ViewModelProvider(context as ExaminationActivity)[ExaminationViewModel::class.java].viewPager
    }
}

class PageIndicationSeekBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr),
    ViewModelRetriever by ViewModelRetrieverImplementation(context) {

    init {
        progress = viewModel.pageIndicationSeekBar.pagePercentage(0, max)
        isEnabled = false  // disables dragging of bar
    }

    fun update(dataSetPosition: Int, scrolledRight: Boolean) {
        val targetProgress: Int = viewModel.pageIndicationSeekBar.pagePercentage(dataSetPosition, max)
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

abstract class PageDependentTextView(context: Context, attr: AttributeSet, private val stringId: Int):
    AppCompatTextView(context, attr),
    ViewModelRetriever by ViewModelRetrieverImplementation(context) {

    init{
        updateText(0)
    }

    abstract fun updateText(viewPagerDataSetPosition: Int)
    protected fun getString(viewPagerDataSetPosition: Int): String = context.resources.getString(stringId, *formatArgs(viewPagerDataSetPosition).toTypedArray())
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
    override fun updateText(viewPagerDataSetPosition: Int){ text = getString(viewPagerDataSetPosition) }
    override fun formatArgs(viewPagerDataSetPosition: Int): List<Int> = listOf(viewModel.dataSet.pageIndex(viewPagerDataSetPosition) + 1, viewModel.dataSet.size)
}