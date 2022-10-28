package com.autocrop.activities.cropping.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import com.autocrop.activities.cropping.CropActivityViewModel
import com.autocrop.utils.android.extensions.activityViewModel
import com.autocrop.utils.android.extensions.ifNotInEditMode

class CroppingProgressBar(context: Context, attr: AttributeSet) :
    ProgressBar(context, attr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            max = activityViewModel<CropActivityViewModel>().nSelectedImages
        }
    }
}