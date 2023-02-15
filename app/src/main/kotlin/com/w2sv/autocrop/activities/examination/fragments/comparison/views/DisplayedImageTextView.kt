package com.w2sv.autocrop.activities.examination.fragments.comparison.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.examination.fragments.comparison.model.DisplayedImage
import com.w2sv.autocrop.ui.views.fadeOut

class DisplayedImageTextView(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context, attributeSet) {

    fun setTextAndShow(displayedImage: DisplayedImage) {
        fadeOutAnimation?.stop()

        text = resources.getString(
            when (displayedImage) {
                DisplayedImage.Screenshot -> R.string.original
                DisplayedImage.Crop -> R.string.cropped
            }
        )

        fadeOutAnimation = fadeOut(
            duration = resources.getLong(R.integer.delay_medium),
            delay = resources.getLong(R.integer.delay_medium_large)
        )
    }

    private var fadeOutAnimation: YoYo.YoYoString? = null
}