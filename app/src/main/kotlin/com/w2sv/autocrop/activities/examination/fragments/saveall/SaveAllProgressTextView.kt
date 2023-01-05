package com.w2sv.autocrop.activities.examination.fragments.saveall

import android.content.Context
import android.util.AttributeSet
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.ui.FractionTextView

class SaveAllProgressTextView(context: Context, attr: AttributeSet) :
    FractionTextView(context, attr) {

    private val viewModel by viewModel<SaveAllFragment.ViewModel>()

    fun update(nominator: Int) {
        super.update(nominator, viewModel.nUnprocessedCrops)
    }
}