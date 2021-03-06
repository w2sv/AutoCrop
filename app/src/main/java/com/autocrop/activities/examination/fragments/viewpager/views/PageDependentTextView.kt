package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.core.text.bold
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.uielements.view.StringResourceEquippedTextView
import com.w2sv.autocrop.R

abstract class PageDependentTextView(context: Context, attr: AttributeSet, stringId: Int):
    StringResourceEquippedTextView(context, attr, stringId),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context) {

    abstract fun updateText(position: Int)
}

class DiscardingStatisticsTextView(context: Context, attr: AttributeSet):
    PageDependentTextView(context, attr, R.string.removed) {

    override fun updateText(position: Int) {
        with(sharedViewModel.dataSet[position]) {
            text = SpannableStringBuilder()
                .append(stringResource)
                .bold { append(" ${discardedPercentage}%") }
                .append("=")
                .bold { append("${discardedFileSize}kb") }
        }
    }
}