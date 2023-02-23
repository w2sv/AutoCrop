package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Display
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.w2sv.androidutils.extensions.requireActivity
import com.w2sv.autocrop.utils.extensions.resolution
import com.w2sv.flowfield.Sketch
import processing.android.PFragment

class FlowFieldLayout(context: Context, attr: AttributeSet) :
    FrameLayout(context, attr) {

    private val activity by lazy {
        context.requireActivity()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            (activity as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .add(
                    id,
                    PFragment(Sketch(activity.getDisplayCompat().resolution()))
                )
                .commitAllowingStateLoss()  // Fixes java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        }
    }
}

private fun Activity.getDisplayCompat(): Display =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        display!!
    else
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay