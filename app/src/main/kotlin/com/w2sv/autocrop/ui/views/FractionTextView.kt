package com.w2sv.autocrop.ui.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.core.common.R.string as Strings

open class FractionTextView(context: Context, attr: AttributeSet) :
    FormattableTextView(
        Strings.fraction,
        context,
        attr
    )
