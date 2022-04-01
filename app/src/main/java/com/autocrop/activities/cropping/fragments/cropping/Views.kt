package com.autocrop.activities.cropping.fragments.cropping

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.utils.android.AbstractViewModelRetriever
import com.autocrop.utils.android.ViewModelRetriever
import com.w2sv.autocrop.R

private class CroppingActivityViewModelRetriever(context: Context):
    AbstractViewModelRetriever<CroppingActivityViewModel, CroppingActivity>(context, CroppingActivityViewModel::class.java)

class CurrentImageNumberTextView(context: Context, attr: AttributeSet?):
    AppCompatTextView(context, attr),
    ViewModelRetriever<CroppingActivityViewModel> by CroppingActivityViewModelRetriever(context) {

    init {
        updateText(0)
    }

    fun updateText(currentImageNumber: Int){
        text = context.resources.getString(R.string.fracture, currentImageNumber, viewModel.nSelectedImages)
    }
}