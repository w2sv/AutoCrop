package com.autocrop.activities.cropping.fragments.cropping

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.w2sv.autocrop.R

class CurrentImageNumberTextView(context: Context, attr: AttributeSet?): AppCompatTextView(context, attr){
    private val viewModel: CroppingActivityViewModel by lazy {
        ViewModelProvider(context as CroppingActivity)[CroppingActivityViewModel::class.java]
    }

    fun updateText(currentImageNumber: Int){
        text = context.resources.getString(R.string.fracture, currentImageNumber, viewModel.nSelectedImages)
    }
}