package com.w2sv.autocrop.activities.crop.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.ui.FractionTextView
import kotlin.properties.Delegates

class ProgressTextView(context: Context, attr: AttributeSet) :
    FractionTextView(context, attr) {

    var max by Delegates.notNull<Int>()

    fun update(nominator: Int) {
        super.update(nominator, max)
    }
}