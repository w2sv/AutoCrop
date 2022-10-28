package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.cropping.CropActivityViewModel
import com.autocrop.ui.views.FractionTextView
import com.autocrop.utils.android.extensions.activityViewModelLazy

class ProgressTextView(context: Context, attr: AttributeSet) :
    FractionTextView(context, attr) {

    private val viewModel by activityViewModelLazy<CropActivityViewModel>()

    fun update(nominator: Int) {
        super.update(nominator, viewModel.nSelectedImages)
    }
}