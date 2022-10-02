package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.ui.elements.view.activityViewModel
import com.autocrop.ui.elements.view.fadeIn
import com.autocrop.ui.elements.view.ifNotInEditMode
import com.autocrop.ui.elements.view.show
import com.w2sv.autocrop.R

class ButtonsLayout(context: Context, attributeSet: AttributeSet):
    RelativeLayout(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            val viewModel = activityViewModel<MainActivityViewModel>()

            if (viewModel.fadeInFlowFieldButtons){
                fadeIn(resources.getInteger(R.integer.duration_fade_in_flowfield_fragment_buttons).toLong())
                viewModel.fadeInFlowFieldButtons = false
            }
            else
                show()
        }
    }
}