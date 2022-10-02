package com.autocrop.ui.elements

import android.content.Context
import android.util.AttributeSet

abstract class FractionTextView(context: Context, attr: AttributeSet)
    : ExtendedAppCompatTextView(context, attr) {

    fun update(nominator: Int, denominator: Int){
        text = template.format(nominator, denominator)
    }
}