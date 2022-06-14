package com.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import com.daimajia.androidanimations.library.Techniques

class W2SVTextView(context: Context, attr: AttributeSet):
    AnimationHandlingTextView(
        context,
        attr,
        Techniques.ZoomOutUp,
        "http://github.com/w2sv"
    )