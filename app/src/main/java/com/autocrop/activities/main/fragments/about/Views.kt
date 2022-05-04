package com.autocrop.activities.main.fragments.about

import android.content.Context
import android.util.AttributeSet
import com.autocrop.uielements.view.StringResourceEquippedTextView
import com.autocrop.uielements.view.animate
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.R
import java.util.*

class VersionTextView(context: Context, attr: AttributeSet):
    StringResourceEquippedTextView(context, attr, R.string.version){

    init {
        text = stringResource.format(BuildConfig.VERSION_NAME)

        setOnClickListener { it.animate(Techniques.RubberBand) }
    }
}

class CopyrightTextView(context: Context, attr: AttributeSet):
    StringResourceEquippedTextView(context, attr, R.string.copyright){

    init { text = stringResource.format(Calendar.getInstance().get(Calendar.YEAR)) }
}