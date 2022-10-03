package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.fragments.flowfield.sketch.FlowFieldSketch
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.utils.android.extensions.ifNotInEditMode
import com.autocrop.utils.android.extensions.resolution
import processing.android.PFragment

class FlowFieldContainer(context: Context, attr: AttributeSet) :
    FrameLayout(context, attr),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

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
