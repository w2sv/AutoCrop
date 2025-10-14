package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View

interface CropAdjustmentViewMode {
    fun initialize()
    fun reset() {}

    /**
     * The mode's touch event handling. This method will be called from the hosting View's [View.onTouchEvent].
     * @return True if the [event] was handled, false otherwise.
     */
    fun onTouchEvent(event: MotionEvent): Boolean
    fun onDraw(canvas: Canvas)
}
