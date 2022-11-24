package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.ui.FractionTextView

class PositionTextView(context: Context, attr: AttributeSet) : FractionTextView(context, attr) {

    private val viewModel by viewModel<CropPagerViewModel>()

    fun update(position: Int) {
        super.update(position, viewModel.dataSet.size)
    }
}