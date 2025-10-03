package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.Canvas
import android.view.MotionEvent

interface CropAdjustmentViewMode {
    fun setUp()
    fun reset() {}
    fun onTouchEvent(event: MotionEvent): Boolean
    fun onDraw(canvas: Canvas)
}
