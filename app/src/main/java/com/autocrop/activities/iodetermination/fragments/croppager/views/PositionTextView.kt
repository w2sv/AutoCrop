package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.ui.views.FractionTextView
import com.autocrop.utils.android.extensions.viewModelLazy

class PositionTextView(context: Context, attr: AttributeSet)
    : FractionTextView(context, attr) {

    private val viewModel by viewModelLazy<CropPagerViewModel>()

    fun update(position: Int){
        super.update(position, viewModel.dataSet.size)
    }
}