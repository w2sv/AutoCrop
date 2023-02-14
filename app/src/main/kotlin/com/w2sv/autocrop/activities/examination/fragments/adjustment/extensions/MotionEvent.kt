package com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions

import android.view.MotionEvent

fun MotionEvent.onHorizontalLine(yLine: Float, touchThreshold: Float): Boolean =
    yLine + touchThreshold > y  && y > yLine - touchThreshold