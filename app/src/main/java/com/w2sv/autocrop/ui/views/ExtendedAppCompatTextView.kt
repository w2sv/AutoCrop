package com.w2sv.autocrop.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

abstract class ExtendedAppCompatTextView(context: Context, attr: AttributeSet) : AppCompatTextView(context, attr) {
    protected val template = text as String
}