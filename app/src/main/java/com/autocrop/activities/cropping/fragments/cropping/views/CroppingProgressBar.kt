package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import com.autocrop.activities.cropping.CropActivityViewModel
import com.autocrop.activities.cropping.fragments.CropActivityViewModelRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever

class CroppingProgressBar(context: Context, attr: AttributeSet):
    ProgressBar(context, attr),
    ViewModelRetriever<CropActivityViewModel> by CropActivityViewModelRetriever(context){

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        max = sharedViewModel.nSelectedImages
    }
}