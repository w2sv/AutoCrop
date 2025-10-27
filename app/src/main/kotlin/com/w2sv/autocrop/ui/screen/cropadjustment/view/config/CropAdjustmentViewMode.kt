package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.Canvas
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

interface CropAdjustmentViewMode {

    /**
     * The mode's touch event handling. This method will be called from the hosting View's [View.onTouchEvent].
     * @return True if the [event] was handled, false otherwise.
     */
    fun onTouchEvent(event: MotionEvent): Boolean
    fun onDraw(canvas: Canvas)
    fun displayCropRect(dstRect: RectF)
}
