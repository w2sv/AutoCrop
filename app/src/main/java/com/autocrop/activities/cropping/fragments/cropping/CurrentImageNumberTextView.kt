package com.autocrop.activities.cropping.fragments.cropping

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.activities.cropping.fragments.CroppingActivityViewModelRetriever
import com.autocrop.utils.android.ExtendedTextView
import com.autocrop.utils.android.ViewModelRetriever
import com.w2sv.autocrop.R

class CurrentImageNumberTextView(context: Context, attr: AttributeSet):
    ExtendedTextView(context, attr, R.string.fracture),
    ViewModelRetriever<CroppingActivityViewModel> by CroppingActivityViewModelRetriever(context) {

    init {
        updateText(0)
    }

    fun updateText(currentImageNumber: Int){
        text = getString().format(currentImageNumber, viewModel.nSelectedImages)
    }
}