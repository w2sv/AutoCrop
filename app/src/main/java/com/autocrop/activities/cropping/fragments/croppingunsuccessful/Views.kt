package com.autocrop.activities.cropping.fragments.croppingunsuccessful

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.autocrop.utils.get
import com.w2sv.autocrop.R

class CroppingFailureTextView(context: Context, attr: AttributeSet?): AppCompatTextView(context, attr){
    fun updateText(multipleScreenshotsAttempted: Boolean){
        val formatArgs = listOf(
            listOf(" any of", "s"),
            listOf("", "")
        )[multipleScreenshotsAttempted]

        text = context.resources.getString(R.string.cropping_failure, formatArgs[0], formatArgs[1])
    }
}