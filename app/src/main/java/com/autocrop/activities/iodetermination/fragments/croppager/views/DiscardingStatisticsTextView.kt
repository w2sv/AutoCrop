package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.core.text.bold
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.utils.android.extensions.activityViewModelLazy
import com.autocrop.ui.views.ExtendedAppCompatTextView

class DiscardingStatisticsTextView(context: Context, attr: AttributeSet):
    ExtendedAppCompatTextView(context, attr) {

    private val viewModel by activityViewModelLazy<CropPagerViewModel>()

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