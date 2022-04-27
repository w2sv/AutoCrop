package com.autocrop.activities.main.fragments.flowfield

import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.autocrop.activities.main.fragments.flowfield.sketch.FlowFieldSketch
import com.autocrop.utils.android.screenResolution
import processing.android.PFragment

class FlowFieldBinding(activity: FragmentActivity,
                       canvasContainer: FrameLayout){

    private val pFragment: PFragment

    init {
        val (w, h) = screenResolution(activity.windowManager).run {x to y}

        pFragment = PFragment(FlowFieldSketch(w, h))
        pFragment.setView(canvasContainer, activity)
    }
}