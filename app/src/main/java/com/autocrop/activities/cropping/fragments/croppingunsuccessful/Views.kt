package com.autocrop.activities.cropping.fragments.croppingunsuccessful

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.activities.cropping.fragments.CroppingActivityViewModelRetriever
import com.autocrop.utils.android.ExtendedTextView
import com.autocrop.utils.android.ViewModelRetriever
import com.autocrop.utils.get
import com.w2sv.autocrop.R

class CroppingUnsuccessfulTextView(context: Context, attr: AttributeSet):
    ExtendedTextView(context, attr, R.string.cropping_failure),
    ViewModelRetriever<CroppingActivityViewModel> by CroppingActivityViewModelRetriever(context)
{
    init {
        updateText(viewModel.nSelectedImages > 1)
    }

    private fun updateText(attemptedMultipleScreenshots: Boolean){
        text = getString().format(
            *listOf(
                listOf("", ""),
                listOf(" any of", "s")
            )[attemptedMultipleScreenshots].toTypedArray()
        )
    }
}