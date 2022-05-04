package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.uicontroller.ViewModelHolder
import com.autocrop.uielements.view.fadeIn
import com.autocrop.uielements.view.show
import com.w2sv.autocrop.R

class ButtonsLayout(context: Context, attributeSet: AttributeSet):
    RelativeLayout(context, attributeSet),
    ViewModelHolder<MainActivityViewModel> by MainActivityModelRetriever(context) {

    init {
        if (sharedViewModel.fadeInFlowFieldButtons){
            fadeIn(resources.getInteger(R.integer.fade_in_duration_flowfield_fragment_buttons).toLong())
            sharedViewModel.fadeInFlowFieldButtons = false
        }
        else
            show()
    }
}