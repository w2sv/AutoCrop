package com.w2sv.autocrop.ui.screen.cropadjustment.extensions

import android.view.MotionEvent

fun MotionEvent.isOnHorizontalLine(yLine: Float, touchThreshold: Float): Boolean =
    yLine + touchThreshold > y && y > yLine - touchThreshold