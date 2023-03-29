package com.w2sv.autocrop.utils.extensions

import android.graphics.Point
import android.os.Build
import android.view.WindowManager

val WindowManager.resolution: Point
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        Point(
            currentWindowMetrics.bounds.width(),
            currentWindowMetrics.bounds.height()
        )
    else
        Point().apply {
            @Suppress("DEPRECATION")
            defaultDisplay.getRealSize(this)
        }