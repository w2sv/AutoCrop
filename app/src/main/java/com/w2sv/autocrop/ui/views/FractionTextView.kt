package com.w2sv.autocrop.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.w2sv.autocrop.utils.android.extensions.textString

abstract class FractionTextView(context: Context, attr: AttributeSet) : AppCompatTextView(context, attr) {

    fun update(nominator: Int, denominator: Int) {
        text = textString.format(nominator, denominator)
    }
}