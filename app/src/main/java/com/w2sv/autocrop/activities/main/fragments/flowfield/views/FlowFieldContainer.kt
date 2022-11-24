package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import com.w2sv.androidutils.ActivityRetriever
import com.w2sv.androidutils.extensions.ifNotInEditMode
import com.w2sv.autocrop.activities.main.fragments.flowfield.views.sketch.FlowFieldSketch
import com.w2sv.autocrop.utils.extensions.resolution
import processing.android.PFragment

class FlowFieldContainer(context: Context, attr: AttributeSet) :
    FrameLayout(context, attr),
    ActivityRetriever by ActivityRetriever.Implementation(context) {

    init {
        ifNotInEditMode {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                activity.display!!
            else
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay

            display.resolution().let {
                PFragment(
                    FlowFieldSketch(
                        it.x,
                        it.y
                    )
                )
                    .setView(this, fragmentActivity)
            }
        }
    }
}
