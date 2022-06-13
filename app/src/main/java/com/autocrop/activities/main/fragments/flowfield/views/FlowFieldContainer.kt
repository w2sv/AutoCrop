package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.fragments.flowfield.sketch.FlowFieldSketch
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.utilsandroid.resolution
import processing.android.PFragment

class FlowFieldContainer(context: Context, attr: AttributeSet) :
    FrameLayout(context, attr),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context) {

    init {
        @Suppress("DEPRECATION")
        with(activity.windowManager.defaultDisplay.resolution()){
            PFragment(FlowFieldSketch(x, y))
                .setView(this@FlowFieldContainer, fragmentActivity)
        }
    }
}