package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.ui.elements.FractionTextView
import com.autocrop.ui.elements.view.activityViewModelLazy
import com.autocrop.ui.elements.view.viewModelLazy

class PositionTextView(context: Context, attr: AttributeSet)
    : FractionTextView(context, attr) {

    private val viewModel by activityViewModelLazy<CropPagerViewModel>()

    fun update(position: Int){
        super.update(position, viewModel.dataSet.size)
    }
}