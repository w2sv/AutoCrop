package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Display
import android.widget.FrameLayout
import com.w2sv.androidutils.ActivityRetriever
import com.w2sv.autocrop.flowfield.FlowFieldSketch
import com.w2sv.autocrop.utils.extensions.resolution
import processing.android.PFragment

class FlowFieldLayout(context: Context, attr: AttributeSet) :
    FrameLayout(context, attr),
    ActivityRetriever by ActivityRetriever.Implementation(context) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            activity.getDisplayCompat().resolution().let {
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

private fun Activity.getDisplayCompat(): Display =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        display!!
    else
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay