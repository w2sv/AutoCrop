package com.autocrop.activities.examination.fragments.viewpager

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
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

    fun update(dataSetPosition: Int) {
        val targetProgress: Int = viewModel.pageIndicationSeekBar.pagePercentage(dataSetPosition, max)
        val traverseEntireBar: Boolean = (setOf(progress, targetProgress) subtract setOf(0, 100)).isEmpty()

        with(
            ObjectAnimator.ofInt(
                this,
                "progress",
                targetProgress
            )
        ) {
            duration = 400
            interpolator = if (traverseEntireBar) BounceInterpolator() else DecelerateInterpolator()
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

    fun updateText(viewPagerDataSetPosition: Int){
        text = context.resources.getString(stringId, *formatArgs(viewPagerDataSetPosition).toTypedArray())
    }
    protected abstract fun formatArgs(viewPagerDataSetPosition: Int): List<Int>
}

class DiscardedTextView(context: Context, attr: AttributeSet):
    PageDependentTextView(context, attr, R.string.discarded) {

    override fun formatArgs(viewPagerDataSetPosition: Int): List<Int> = listOf(viewModel.dataSet[viewPagerDataSetPosition].discardedPercentage, viewModel.dataSet[viewPagerDataSetPosition].approximateDiscardedFileSize)
}

class PageIndicationTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr, R.string.fracture) {
    override fun formatArgs(viewPagerDataSetPosition: Int): List<Int> = listOf(viewModel.dataSet.pageIndex(viewPagerDataSetPosition) + 1, viewModel.dataSet.size)
}