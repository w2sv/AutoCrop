package com.w2sv.autocrop.activities.iodetermination.fragments.comparison.views

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.iodetermination.fragments.comparison.ComparisonViewModel
import com.w2sv.autocrop.utils.android.extensions.fadeOut
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.ifNotInEditMode
import com.w2sv.autocrop.utils.android.extensions.show
import com.w2sv.autocrop.utils.android.extensions.viewModelImmediate

class ComparisonIVStatusTV(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            viewModelImmediate<ComparisonViewModel>().displayScreenshot.observe(findViewTreeLifecycleOwner()!!) {
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

//    override fun onSaveInstanceState(): Parcelable? {
//        fadeOutAnimation?.stop()
//        fadeOutAnimation = null
//
//        return super.onSaveInstanceState()
//    }

    private var fadeOutAnimation: YoYo.YoYoString? = null
}