package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.activities.cropping.fragments.CroppingActivityViewModelRetrievingView
import com.autocrop.uielements.view.StringResourceCoupledTextView
import com.autocrop.uielements.view.ViewModelRetriever
import com.w2sv.autocrop.R

class CurrentImageNumberTextView(context: Context, attr: AttributeSet):
    StringResourceCoupledTextView(context, attr, R.string.fracture),
    ViewModelRetriever<CroppingActivityViewModel> by CroppingActivityViewModelRetrievingView(context) {

    init {
        updateText(0)
    }

    fun updateText(currentImageNumber: Int){
        text = stringResource.format(currentImageNumber, viewModel.nSelectedImages)
    }
}