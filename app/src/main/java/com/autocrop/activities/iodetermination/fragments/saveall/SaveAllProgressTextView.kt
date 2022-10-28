package com.autocrop.activities.iodetermination.fragments.saveall

import android.content.Context
import android.util.AttributeSet
import com.autocrop.ui.views.FractionTextView
import com.autocrop.utils.android.extensions.viewModelLazy

class SaveAllProgressTextView(context: Context, attr: AttributeSet):
    FractionTextView(context, attr) {

    private val viewModel by viewModelLazy<SaveAllViewModel>()

    fun update(nominator: Int) {
        super.update(nominator, viewModel.nImagesToBeSaved)
    }
}