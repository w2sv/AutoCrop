package com.autocrop.utils.android

import android.graphics.Point
import android.view.WindowManager


fun screenResolution(windowManager: WindowManager): Point = Point().apply {
    windowManager.defaultDisplay.getRealSize(this)
}