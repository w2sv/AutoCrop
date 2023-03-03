package com.w2sv.autocrop.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView

abstract class FormattableTextView(
    @StringRes private val string: Int,
    context: Context,
    attr: AttributeSet
) : AppCompatTextView(context, attr) {
    fun updateText(vararg formatArgs: Any) {
        text = resources.getString(string, *formatArgs)
    }
}