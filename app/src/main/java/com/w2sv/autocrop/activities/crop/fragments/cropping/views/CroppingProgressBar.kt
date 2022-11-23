package com.w2sv.autocrop.activities.crop.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.utils.android.extensions.activityViewModelImmediate
import com.w2sv.autocrop.utils.android.extensions.ifNotInEditMode

class CroppingProgressBar(context: Context, attr: AttributeSet) :
    ProgressBar(context, attr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            max = activityViewModelImmediate<CropActivity.ViewModel>().nImages
        }
    }
}