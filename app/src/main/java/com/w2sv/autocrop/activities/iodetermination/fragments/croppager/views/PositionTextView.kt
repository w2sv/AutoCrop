package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.ui.views.FractionTextView
import com.w2sv.autocrop.utils.android.extensions.viewModel

class PositionTextView(context: Context, attr: AttributeSet) : FractionTextView(context, attr) {

    private val viewModel by viewModel<CropPagerViewModel>()

    fun update(position: Int) {
        super.update(position, viewModel.dataSet.size)
    }
}