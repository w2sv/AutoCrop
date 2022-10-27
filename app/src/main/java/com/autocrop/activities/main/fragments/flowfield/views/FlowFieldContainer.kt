package com.autocrop.activities.main.fragments.flowfield.views

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import com.autocrop.activities.main.fragments.flowfield.sketch.FlowFieldSketch
import com.autocrop.controller.activity.retriever.ActivityRetriever
import com.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.autocrop.utils.android.extensions.ifNotInEditMode
import com.autocrop.utils.android.extensions.resolution
import processing.android.PFragment

class FlowFieldContainer(context: Context, attr: AttributeSet) :
    FrameLayout(context, attr),
    ActivityRetriever<Activity> by ContextBasedActivityRetriever(context) {

    init {
        ifNotInEditMode {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                activity.display!!
            else
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay

            display.resolution().let{
                PFragment(FlowFieldSketch(it.x, it.y))
                    .setView(this, fragmentActivity)
            }
        }
    }
}
