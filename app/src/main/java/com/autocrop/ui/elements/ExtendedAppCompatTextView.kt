package com.autocrop.ui.elements

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.w2sv.autocrop.BuildConfig

abstract class ExtendedAppCompatTextView(context: Context, attr: AttributeSet)
    : AppCompatTextView(context, attr) {
    protected val template = text as String
}