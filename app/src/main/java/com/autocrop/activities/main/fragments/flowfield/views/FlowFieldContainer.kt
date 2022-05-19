package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.fragments.flowfield.sketch.FlowFieldSketch
import com.autocrop.uielements.view.ActivityRetriever
import com.autocrop.uielements.view.ContextBasedActivityRetriever
import com.autocrop.utilsandroid.screenResolution
import processing.android.PFragment

class FlowFieldContainer(context: Context, attr: AttributeSet) :
    FrameLayout(context, attr),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context) {

    init {
        with(screenResolution(activity.windowManager)){
            PFragment(FlowFieldSketch(x, y)).setView(this@FlowFieldContainer, fragmentActivity)
        }
    }
}