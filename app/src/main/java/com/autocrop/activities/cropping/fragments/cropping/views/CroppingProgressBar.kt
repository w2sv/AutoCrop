package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.activities.cropping.fragments.CroppingActivityViewModelRetrievingView
import com.autocrop.uielements.view.ViewModelRetriever

class CroppingProgressBar(context: Context, attr: AttributeSet):
    ProgressBar(context, attr),
    ViewModelRetriever<CroppingActivityViewModel> by CroppingActivityViewModelRetrievingView(context){
        init {
            max = viewModel.nSelectedImages
        }
    }