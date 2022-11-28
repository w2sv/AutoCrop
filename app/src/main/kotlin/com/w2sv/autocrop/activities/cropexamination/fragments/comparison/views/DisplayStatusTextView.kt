package com.w2sv.autocrop.activities.cropexamination.fragments.comparison.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.cropexamination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.ui.fadeOut

class DisplayStatusTextView(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode){
            viewModel<ComparisonFragment.ViewModel>().value.displayScreenshotLive.observe(findViewTreeLifecycleOwner()!!) {
                fadeOutAnimation?.stop()

                text = resources.getString(if (it) R.string.screenshot else R.string.crop)
                show()
                fadeOutAnimation = fadeOut(
                    duration = resources.getLong(R.integer.delay_medium),
                    delay = resources.getLong(R.integer.delay_large)
                )
            }
        }
    }

    private var fadeOutAnimation: YoYo.YoYoString? = null
}