package com.autocrop.activities.cropping.fragments.croppingunsuccessful

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.TintTypedArray.obtainStyledAttributes
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.utils.get
import com.w2sv.autocrop.R

class CroppingUnsuccessfulTextView(context: Context, attr: AttributeSet?): AppCompatTextView(context, attr){
    private val viewModel: CroppingActivityViewModel = ViewModelProvider(context as CroppingActivity)[CroppingActivityViewModel::class.java]

    init {
        updateText(viewModel.nSelectedImages > 1)
    }

    fun updateText(multipleScreenshotsAttempted: Boolean){
        val formatArgs = listOf(
            listOf("", ""),
            listOf(" any of", "s")
        )[multipleScreenshotsAttempted]

        text = context.resources.getString(R.string.cropping_failure, formatArgs[0], formatArgs[1])
    }
}