package com.autocrop.utils.android

import android.graphics.Point
import android.view.Display

/**
 * Raw size of current default display irrespective of window decor,
 * however adjusted based on display rotation
 */
fun Display.resolution(): Point =
    Point().apply {
        @Suppress("DEPRECATION")
        getRealSize(this)
    }