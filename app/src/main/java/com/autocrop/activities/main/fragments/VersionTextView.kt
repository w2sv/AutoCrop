package com.autocrop.activities.main.fragments

import android.content.Context
import android.util.AttributeSet
import com.autocrop.ui.views.ExtendedAppCompatTextView
import com.w2sv.autocrop.BuildConfig

class VersionTextView(context: Context, attr: AttributeSet):
    ExtendedAppCompatTextView(context, attr){

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        text = template.format(BuildConfig.VERSION_NAME)
    }
}