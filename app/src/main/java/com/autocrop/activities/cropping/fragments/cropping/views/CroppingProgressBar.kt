package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.activities.cropping.fragments.CroppingActivityViewModelHoldingView
import com.autocrop.uicontroller.ViewModelHolder

class CroppingProgressBar(context: Context, attr: AttributeSet):
    ProgressBar(context, attr),
    ViewModelHolder<CroppingActivityViewModel> by CroppingActivityViewModelHoldingView(context){
        init {
            max = sharedViewModel.nSelectedImages
        }
    }