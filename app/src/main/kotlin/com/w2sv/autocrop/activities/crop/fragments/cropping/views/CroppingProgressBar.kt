package com.w2sv.autocrop.activities.crop.fragments.cropping.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import com.w2sv.androidutils.extensions.hiltActivityViewModel
import com.w2sv.androidutils.extensions.ifNotInEditMode
import com.w2sv.autocrop.activities.crop.CropActivity

class CroppingProgressBar(context: Context, attr: AttributeSet) :
    ProgressBar(context, attr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            max = hiltActivityViewModel<CropActivity.ViewModel>().value.nImages
        }
    }
}