package com.autocrop.uielements.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

abstract class StringResourceEquippedTextView(context: Context, attr: AttributeSet, private val stringId: Int)
    : AppCompatTextView(context, attr) {

    protected val stringResource: String
        get() = context.resources.getString(stringId)
}