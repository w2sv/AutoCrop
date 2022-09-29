package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.cropping.CropActivityViewModel
import com.autocrop.activities.cropping.fragments.CropActivityViewModelRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.ui.elements.ExtendedAppCompatTextView

class CurrentImageNumberTextView(context: Context, attr: AttributeSet):
    ExtendedAppCompatTextView(context, attr),
    ViewModelRetriever<CropActivityViewModel> by CropActivityViewModelRetriever(context) {

    fun updateText(currentImageNumber: Int){
        text = template.format(currentImageNumber, sharedViewModel.nSelectedImages)
    }
}