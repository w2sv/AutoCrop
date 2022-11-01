package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.core.text.bold
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.ui.views.ExtendedAppCompatTextView
import com.w2sv.autocrop.utils.android.extensions.viewModel

class DiscardingStatisticsTextView(context: Context, attr: AttributeSet) :
    ExtendedAppCompatTextView(context, attr) {

    private val viewModel by viewModel<CropPagerViewModel>()

    fun update(position: Int) {
        with(viewModel.dataSet[position].crop) {
            text = SpannableStringBuilder()
                .append(template)
                .bold { append(" ${discardedPercentage}%") }
                .append("=")
                .bold { append(discardedFileSizeFormatted) }
        }
    }
}