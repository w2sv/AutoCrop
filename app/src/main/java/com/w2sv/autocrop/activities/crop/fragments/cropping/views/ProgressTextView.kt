package com.w2sv.autocrop.activities.crop.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.ui.views.FractionTextView
import com.w2sv.autocrop.utils.android.extensions.activityViewModel

class ProgressTextView(context: Context, attr: AttributeSet) :
    FractionTextView(context, attr) {

    private val viewModel by activityViewModel<CropActivity.ViewModel>()

    fun update(nominator: Int) {
        super.update(nominator, viewModel.nImages)
    }
}