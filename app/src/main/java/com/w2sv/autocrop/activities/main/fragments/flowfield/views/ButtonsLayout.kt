package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.utils.android.extensions.activityViewModel
import com.w2sv.autocrop.utils.android.extensions.fadeIn
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.ifNotInEditMode
import com.w2sv.autocrop.utils.android.extensions.show

class ButtonsLayout(context: Context, attributeSet: AttributeSet) :
    RelativeLayout(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            with(activityViewModel<FlowFieldFragment.ViewModel>()) {
                if (!enteredFragment)
                    fadeIn(resources.getLong(R.integer.duration_flowfield_buttons_fade_in))
                else
                    show()
            }
        }
    }
}