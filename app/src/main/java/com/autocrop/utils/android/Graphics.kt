package com.autocrop.utils.android

import android.graphics.Point
import android.os.Build
import android.view.WindowManager

@Suppress("DEPRECATION")
fun screenResolution(windowManager: WindowManager): Point =
    Point().apply {
        windowManager.defaultDisplay.getRealSize(this)
    }