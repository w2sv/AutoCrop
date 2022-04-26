package com.autocrop.activities.cropping.fragments.cropping

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
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

class CroppingProgressBar(context: Context, attr: AttributeSet):
    ProgressBar(context, attr),
    ViewModelRetriever<CroppingActivityViewModel> by CroppingActivityViewModelRetrievingView(context){
        init {
            max = viewModel.nSelectedImages
        }
    }