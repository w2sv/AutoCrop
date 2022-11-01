package com.w2sv.autocrop.activities.iodetermination.fragments.saveall

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.ui.views.FractionTextView
import com.w2sv.autocrop.utils.android.extensions.viewModel

class SaveAllProgressTextView(context: Context, attr: AttributeSet) :
    FractionTextView(context, attr) {

    private val viewModel by viewModel<SaveAllViewModel>()

    fun update(nominator: Int) {
        super.update(nominator, viewModel.nImagesToBeSaved)
    }
}