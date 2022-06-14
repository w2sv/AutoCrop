package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.activities.cropping.fragments.CroppingActivityViewModelRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever

class CroppingProgressBar(context: Context, attr: AttributeSet):
    ProgressBar(context, attr),
    ViewModelRetriever<CroppingActivityViewModel> by CroppingActivityViewModelRetriever(context){

    init { max = sharedViewModel.nSelectedImages }
}