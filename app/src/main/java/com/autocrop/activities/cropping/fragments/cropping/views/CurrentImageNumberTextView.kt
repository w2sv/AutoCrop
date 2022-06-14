package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.activities.cropping.fragments.CroppingActivityViewModelRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.uielements.ExtendedAppCompatTextView

class CurrentImageNumberTextView(context: Context, attr: AttributeSet):
    ExtendedAppCompatTextView(context, attr),
    ViewModelRetriever<CroppingActivityViewModel> by CroppingActivityViewModelRetriever(context) {

    fun updateText(currentImageNumber: Int){
        text = template.format(currentImageNumber, sharedViewModel.nSelectedImages)
    }
}