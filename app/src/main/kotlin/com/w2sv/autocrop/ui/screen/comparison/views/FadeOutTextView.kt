package com.w2sv.autocrop.ui.screen.comparison.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.view.show
import com.w2sv.autocrop.ui.util.view.fadeOut

class FadeOutTextView(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context, attributeSet) {

    fun setAndShow(args: Args) {
        fadeOutAnimation?.stop()

        text = resources.getString(args.textRes)
        setCompoundDrawablesWithIntrinsicBounds(
            args.iconRes?.let { ContextCompat.getDrawable(context, it) },
            null,
            null,
            null
        )
        show()

        fadeOutAnimation = fadeOut(
            duration = 500,
            delay = args.displayDuration
        )
    }

    private var fadeOutAnimation: YoYo.YoYoString? = null

    data class Args(@StringRes val textRes: Int, @DrawableRes val iconRes: Int? = null, val displayDuration: Long = 750)
}
