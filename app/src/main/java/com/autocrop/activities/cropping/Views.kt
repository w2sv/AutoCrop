package com.autocrop.activities.cropping

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModelProvider
import com.autocrop.utils.get
import com.w2sv.autocrop.R

class CurrentImageNumberTextView(context: Context, attr: AttributeSet?): AppCompatTextView(context, attr){
    private val viewModel: CroppingActivityViewModel = ViewModelProvider(context as CroppingActivity)[CroppingActivityViewModel::class.java]

    fun updateText(currentImageNumber: Int){
        text = context.resources.getString(R.string.fracture, currentImageNumber, viewModel.nSelectedImages)
    }
}

class CroppingFailureTextView(context: Context, attr: AttributeSet?): AppCompatTextView(context, attr){
    fun updateText(multipleScreenshotsAttempted: Boolean){
        val formatArgs = listOf(
            listOf(" any of", "s"),
            listOf("", "")
        )[multipleScreenshotsAttempted]

        text = context.resources.getString(R.string.cropping_failure, formatArgs[0], formatArgs[1])
    }
}