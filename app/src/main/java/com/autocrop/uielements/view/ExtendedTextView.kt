package com.autocrop.uielements.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

abstract class ExtendedTextView(context: Context, attr: AttributeSet, private val stringId: Int)
    : AppCompatTextView(context, attr) {

    protected fun getString(): String = context.resources.getString(stringId)
}