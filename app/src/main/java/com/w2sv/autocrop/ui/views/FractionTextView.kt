package com.w2sv.autocrop.ui.views

import android.content.Context
import android.util.AttributeSet

abstract class FractionTextView(context: Context, attr: AttributeSet) : ExtendedAppCompatTextView(context, attr) {

    fun update(nominator: Int, denominator: Int) {
        text = template.format(nominator, denominator)
    }
}