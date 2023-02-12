package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Display
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.w2sv.androidutils.ActivityRetriever
import com.w2sv.autocrop.utils.extensions.resolution
import com.w2sv.flowfield.Sketch
import processing.android.PFragment

class FlowFieldLayout(context: Context, attr: AttributeSet) :
    FrameLayout(context, attr),
    ActivityRetriever by ActivityRetriever.Impl(context) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            activity.getDisplayCompat().resolution().let {
                (activity as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .add(
                        id,
                        PFragment(
                            Sketch(
                                it.x,
                                it.y
                            )
                        )
                    )
                    .commitAllowingStateLoss()  // Fixes Exception java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
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