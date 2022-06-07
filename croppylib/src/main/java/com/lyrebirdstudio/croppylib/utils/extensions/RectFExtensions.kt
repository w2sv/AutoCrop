package com.lyrebirdstudio.croppylib.utils.extensions

import android.graphics.RectF
import android.view.MotionEvent
import com.lyrebirdstudio.croppylib.utils.model.Corner
import com.lyrebirdstudio.croppylib.utils.model.Edge
import kotlin.math.hypot

fun RectF.getEdgeTouch(touchEvent: MotionEvent, touchThreshold: Float = 50f): Edge {
    val isLeft = touchEvent.x < left + touchThreshold &&
            touchEvent.x > left - touchThreshold &&
            touchEvent.y > top &&
            touchEvent.y < bottom

    val isRight = touchEvent.x < right + touchThreshold &&
            touchEvent.x > right - touchThreshold &&
            touchEvent.y > top &&
            touchEvent.y < bottom

    val isTop = touchEvent.x < right &&
            touchEvent.x > left &&
            touchEvent.y < top + touchThreshold &&
            touchEvent.y > top - touchThreshold

    val isBottom = touchEvent.x < right &&
            touchEvent.x > left &&
            touchEvent.y < bottom + touchThreshold &&
            touchEvent.y > bottom - touchThreshold

    return when {
        isLeft -> Edge.LEFT
        isRight -> Edge.RIGHT
        isTop -> Edge.TOP
        isBottom -> Edge.BOTTOM
        else -> Edge.NONE
    }
}

fun RectF.getCornerTouch(touchEvent: MotionEvent, touchThreshold: Float = 50f): Corner {
    val isTopLeft =
        touchEvent.y < top + touchThreshold &&
                touchEvent.y > top - touchThreshold &&
                touchEvent.x < left + touchThreshold &&
                touchEvent.x > left - touchThreshold

    val isTopRight = touchEvent.y < top + touchThreshold &&
            touchEvent.y > top - touchThreshold &&
            touchEvent.x < right + touchThreshold &&
            touchEvent.x > right - touchThreshold

    val isBottomLeft = touchEvent.y < bottom + touchThreshold &&
            touchEvent.y > bottom - touchThreshold &&
            touchEvent.x < left + touchThreshold &&
            touchEvent.x > left - touchThreshold

    val isBottomRight = touchEvent.y < bottom + touchThreshold &&
            touchEvent.y > bottom - touchThreshold &&
            touchEvent.x < right + touchThreshold &&
            touchEvent.x > right - touchThreshold

    return when {
        isTopLeft -> Corner.TOP_LEFT
        isTopRight -> Corner.TOP_RIGHT
        isBottomLeft -> Corner.BOTTOM_LEFT
        isBottomRight -> Corner.BOTTOM_RIGHT
        else -> Corner.NONE
    }
}

fun MotionEvent.withinRectangle(rectF: RectF): Boolean =
    x > rectF.left && x < rectF.right && y > rectF.top && y < rectF.bottom

fun RectF.getHypotenus(): Float {
    return hypot(height().toDouble(), width().toDouble()).toFloat()
}