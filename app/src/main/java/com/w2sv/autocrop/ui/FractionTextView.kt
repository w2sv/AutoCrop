package com.w2sv.autocrop.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.w2sv.autocrop.R

abstract class FractionTextView(context: Context, attr: AttributeSet) : AppCompatTextView(context, attr) {

    fun update(nominator: Int, denominator: Int) {
        text = resources.getString(R.string.fraction, nominator, denominator)
    }
}