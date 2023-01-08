package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Display
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.w2sv.androidutils.ActivityRetriever
import com.w2sv.autocrop.flowfield.Sketch
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
                    Sketch(
                        it.x,
                        it.y
                    )
                )
                    .setViewAllowingStateLoss(this, fragmentActivity)
            }
        }
    }
}

/**
 * Fixes Exception java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
 */
private fun PFragment.setViewAllowingStateLoss(view: View, fragmentActivity: FragmentActivity){
    fragmentActivity.supportFragmentManager
        .beginTransaction()
        .add(view.id, this)
        .commitAllowingStateLoss()
}

private fun Activity.getDisplayCompat(): Display =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        display!!
    else
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay