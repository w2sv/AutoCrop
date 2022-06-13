package com.autocrop.utilsandroid

import android.graphics.Point
import android.view.Display
import android.view.MotionEvent
import android.view.WindowManager
import kotlin.math.abs

/**
 * Raw size of current default display irrespective of window decor,
 * however adjusted based on display rotation
 */
fun Display.resolution(): Point =
    Point().apply {
        @Suppress("DEPRECATION")
        getRealSize(this)
    }