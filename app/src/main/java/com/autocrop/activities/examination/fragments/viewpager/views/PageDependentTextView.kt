package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.core.text.bold
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragmentViewModel
import com.autocrop.uielements.view.StringResourceCoupledTextView
import com.autocrop.uielements.view.ViewModelRetriever
import com.autocrop.uielements.view.show
import com.w2sv.autocrop.R

abstract class PageDependentTextView(context: Context, attr: AttributeSet, stringId: Int):
    StringResourceCoupledTextView(context, attr, stringId),
    ViewModelRetriever<ViewPagerFragmentViewModel> by ViewPagerViewModelRetriever(context) {

    abstract fun updateText(position: Int)
}

class DiscardedTextView(context: Context, attr: AttributeSet):
    PageDependentTextView(context, attr, R.string.discarded) {

    init {
        if (!viewModel.autoScroll)
            show()
    }

    override fun updateText(position: Int) {
        with(viewModel.dataSet[position]) {
            text = SpannableStringBuilder()
                .append(stringResource)
                .bold { append(" ${discardedPercentage}%") }
                .append("=")
                .bold { append("${approximateDiscardedFileSize}kb") }
        }
    }
}