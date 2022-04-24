package com.autocrop.utils.android

import android.graphics.Point
import android.os.Build
import android.view.WindowManager

/**
 * Raw size of current default display irrespective of window decor,
 * however adjusted based on display rotation
 */
@Suppress("DEPRECATION")
fun screenResolution(windowManager: WindowManager): Point =
    Point().apply {
        windowManager.defaultDisplay.getRealSize(this)
    }