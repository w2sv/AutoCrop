package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.utils.android.extensions.activityViewModel
import com.autocrop.utils.android.extensions.fadeIn
import com.autocrop.utils.android.extensions.ifNotInEditMode
import com.autocrop.utils.android.extensions.show
import com.w2sv.autocrop.R

class ButtonsLayout(context: Context, attributeSet: AttributeSet):
    RelativeLayout(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            with(activityViewModel<MainActivityViewModel>()){
                if (fadeInFlowFieldButtons){
                    fadeIn(resources.getInteger(R.integer.duration_fade_in_flowfield_fragment_buttons).toLong())
                    fadeInFlowFieldButtons = false
                }
                else
                    show()
            }
        }
    }
}