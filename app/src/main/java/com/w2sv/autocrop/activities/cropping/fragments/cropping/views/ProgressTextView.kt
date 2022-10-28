package com.w2sv.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.activities.cropping.CropActivityViewModel
import com.w2sv.autocrop.ui.views.FractionTextView
import com.w2sv.autocrop.utils.android.extensions.activityViewModelLazy

class ProgressTextView(context: Context, attr: AttributeSet) :
    FractionTextView(context, attr) {

    private val viewModel by activityViewModelLazy<CropActivityViewModel>()

    fun update(nominator: Int) {
        super.update(nominator, viewModel.nSelectedImages)
    }
}