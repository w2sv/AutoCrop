package com.w2sv.autocrop.activities.examination.fragments.comparison.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.ui.resources.getLong
import com.w2sv.androidutils.ui.views.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.examination.fragments.comparison.model.ImageType
import com.w2sv.autocrop.ui.views.fadeOut

class ImageTypeLabelTextView(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context, attributeSet) {

    fun setTextAndShow(imageType: ImageType) {
        fadeOutAnimation?.stop()

        text = resources.getString(imageType.labelRes)
        show()

        fadeOutAnimation = fadeOut(
            duration = resources.getLong(R.integer.delay_medium),
            delay = resources.getLong(R.integer.delay_medium_large)
        )
    }

    private var fadeOutAnimation: YoYo.YoYoString? = null
}