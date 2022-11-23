package com.w2sv.autocrop.activities.main.fragments

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.utils.android.extensions.textString

class VersionTextView(context: Context, attr: AttributeSet) :
    AppCompatTextView(context, attr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        text = textString.format(BuildConfig.VERSION_NAME)
    }
}