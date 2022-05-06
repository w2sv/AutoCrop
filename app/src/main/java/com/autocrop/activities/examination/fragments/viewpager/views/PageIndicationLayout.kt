package com.autocrop.activities.examination.fragments.viewpager.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.BaseInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatSeekBar
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.uicontroller.ViewModelHolder
import com.w2sv.autocrop.R
import kotlin.math.roundToInt

class PageIndicationBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr),
    ViewModelHolder<ViewPagerViewModel> by ViewPagerViewModelRetriever(context) {

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

        progress(dataSetPosition)?.let { newProgress ->
            with(ObjectAnimator.ofInt(this,"progress", newProgress)) {
                with(interpolator(scrolledRight, newProgress)){
                    interpolator = this
                    duration = animationDuration.getValue(javaClass)
                }
                start()
            }
        }
    }

    private fun progress(pageIndex: Int): Int? =
        if (sharedViewModel.dataSet.containsSingleElement)
            null
        else
            (max.toFloat() / (sharedViewModel.dataSet.lastIndex).toFloat() * pageIndex).roundToInt()

    private fun interpolator(scrolledRight: Boolean, newProgress: Int): BaseInterpolator =
        if ((progress == 0 && newProgress == 100 && !scrolledRight) || progress == 100 && newProgress == 0 && scrolledRight)
            BounceInterpolator()
        else
            DecelerateInterpolator()
}

class PageIndicationTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr, R.string.fracture) {
    override fun updateText(position: Int){
        text = stringResource.format(position + 1, sharedViewModel.dataSet.size)
    }
}