package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.views

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.bold
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.utils.android.extensions.viewModel

class DiscardingStatisticsTextView(context: Context, attr: AttributeSet) :
    AppCompatTextView(context, attr) {

    private val viewModel by viewModel<CropPagerViewModel>()

    fun update(position: Int) {
        with(viewModel.dataSet[position].crop) {
            text = SpannableStringBuilder()
                .append(resources.getString(R.string.remove))
                .bold { append(" ${discardedPercentage}%") }
                .append("=")
                .bold { append(discardedFileSizeFormatted) }
        }
    }
}