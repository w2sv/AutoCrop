package com.w2sv.autocrop.activities.examination.fragments.comparison.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.fadeOut

class DisplayStatusTextView(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context, attributeSet) {

    fun setTextAndShow(displayScreenshot: Boolean) {
        fadeOutAnimation?.stop()

        text = resources.getString(if (displayScreenshot) R.string.screenshot else R.string.crop)
        show()
        fadeOutAnimation = fadeOut(
            duration = resources.getLong(R.integer.delay_medium),
            delay = resources.getLong(R.integer.delay_large)
        )
    }

    private var fadeOutAnimation: YoYo.YoYoString? = null
}