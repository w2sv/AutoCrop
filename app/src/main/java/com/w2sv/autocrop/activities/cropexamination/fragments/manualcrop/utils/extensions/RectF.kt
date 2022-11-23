package com.w2sv.autocrop.activities.cropexamination.fragments.manualcrop.utils.extensions

import android.graphics.RectF
import android.view.MotionEvent
import com.w2sv.autocrop.activities.cropexamination.fragments.manualcrop.utils.model.Corner
import com.w2sv.autocrop.activities.cropexamination.fragments.manualcrop.utils.model.Edge
import java.lang.Float.min
import kotlin.math.max

fun maxRectFFrom(a: RectF, b: RectF): RectF =
    RectF(
        max(a.left, b.left),
        max(a.top, b.top),
        min(a.right, b.right),
        min(a.bottom, b.bottom)
    )

fun minRectFFrom(a: RectF, b: RectF) =
    RectF(
        min(a.left, b.left),
        min(a.top, b.top),
        max(a.right, b.right),
        max(a.bottom, b.bottom)
    )

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
    rectF.contains(x, y)