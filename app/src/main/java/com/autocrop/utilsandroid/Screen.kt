package com.autocrop.utilsandroid

import android.graphics.Point
import android.view.MotionEvent
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

fun isClick(a: MotionEvent, b: MotionEvent): Boolean{
    val clickIdentificationCoordinateThreshold = 100

    return manhattanNorm(a, b) < clickIdentificationCoordinateThreshold
}

private fun manhattanNorm(a: MotionEvent, b: MotionEvent): Float =
    abs(a.x - b.x) + abs(a.y - b.y)