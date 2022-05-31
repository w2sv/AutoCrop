package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.activities.cropping.fragments.CroppingActivityViewModelRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.uielements.view.StringResourceEquippedTextView
import com.w2sv.autocrop.R

class CurrentImageNumberTextView(context: Context, attr: AttributeSet):
    StringResourceEquippedTextView(context, attr, R.string.fracture),
    ViewModelRetriever<CroppingActivityViewModel> by CroppingActivityViewModelRetriever(context) {

    init {
        updateText(0)
    }

    fun updateText(currentImageNumber: Int){
        text = stringResource.format(currentImageNumber, sharedViewModel.nSelectedImages)
    }
}