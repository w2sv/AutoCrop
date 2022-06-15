package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.core.text.bold

class DiscardingStatisticsTextView(context: Context, attr: AttributeSet):
    PageDependentTextView(context, attr) {

    override fun update(position: Int) {
        with(sharedViewModel.dataSet[position]) {
            text = SpannableStringBuilder()
                .append(template)
                .bold { append(" ${discardedPercentage}%") }
                .append("=")
                .bold { append("${discardedFileSize}kb") }
        }
    }
}