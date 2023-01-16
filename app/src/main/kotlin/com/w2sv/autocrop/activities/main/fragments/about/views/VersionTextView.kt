package com.w2sv.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.R

class VersionTextView(context: Context, attr: AttributeSet) :
    AppCompatTextView(context, attr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        text = resources.getString(R.string.version, BuildConfig.VERSION_NAME)
    }
}