package com.w2sv.autocrop.activities.main.fragments

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.ui.views.ExtendedAppCompatTextView

class VersionTextView(context: Context, attr: AttributeSet) :
    ExtendedAppCompatTextView(context, attr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        text = template.format(BuildConfig.VERSION_NAME)
    }
}