package com.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import com.daimajia.androidanimations.library.Techniques
import java.util.Calendar

class CopyrightTextView(context: Context, attr: AttributeSet) :
    AnimationHandlingTextView(
        context,
        attr,
        Techniques.ZoomOutRight,
        "https://github.com/w2sv/AutoCrop/blob/master/LICENSE"
    ) { init {
    text = template.format(Calendar.getInstance().get(Calendar.YEAR))
}
}