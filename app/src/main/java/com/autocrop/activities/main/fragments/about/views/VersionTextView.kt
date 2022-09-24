package com.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.ui.elements.ExtendedAppCompatTextView
import com.autocrop.ui.elements.view.animate
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.BuildConfig

class VersionTextView(context: Context, attr: AttributeSet):
    ExtendedAppCompatTextView(context, attr){

    init {
        text = template.format(BuildConfig.VERSION_NAME)

        setOnClickListener { it.animate(Techniques.RubberBand) }
    }
}