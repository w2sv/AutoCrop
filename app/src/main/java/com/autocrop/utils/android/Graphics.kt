package com.autocrop.utils.android

import android.graphics.Point
import android.view.WindowManager
import kotlin.math.abs

/**
 * Raw size of current default display irrespective of window decor,
 * however adjusted based on display rotation
 */
@Suppress("DEPRECATION")
fun screenResolution(windowManager: WindowManager): Point =
    Point().apply {
        windowManager.defaultDisplay.getRealSize(this)
    }

fun manhattanNorm(a: Point, b: Point): Int =
    abs(a.x - b.x) + abs(a.y - b.y)