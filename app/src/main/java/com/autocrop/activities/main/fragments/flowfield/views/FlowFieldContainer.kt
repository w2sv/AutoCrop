package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.autocrop.activities.main.fragments.flowfield.sketch.FlowFieldSketch
import com.autocrop.utils.android.screenResolution
import processing.android.PFragment

class FlowFieldContainer(context: Context, attr: AttributeSet)
    : FrameLayout(context, attr){

    init {
        val activity = (context as FragmentActivity)

        val screenResolution = screenResolution(activity.windowManager)
        PFragment(FlowFieldSketch(screenResolution.x, screenResolution.y)).setView(this, activity)
    }
}