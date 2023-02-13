package com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions

import android.graphics.RectF
import android.view.MotionEvent
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge
import com.w2sv.cropbundle.cropping.CropEdges
import java.lang.Float.min
import kotlin.math.max

fun maxRectOf(a: RectF, b: RectF): RectF =
    RectF(
        max(a.left, b.left),
        max(a.top, b.top),
        min(a.right, b.right),
        min(a.bottom, b.bottom)
    )

fun minRectOf(a: RectF, b: RectF) =
    RectF(
        min(a.left, b.left),
        min(a.top, b.top),
        max(a.right, b.right),
        max(a.bottom, b.bottom)
    )

fun RectF.getEdgeTouch(touchEvent: MotionEvent, touchThreshold: Float): Edge? {
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
        else -> null
    }
}

fun MotionEvent.isWithinRectangle(rectF: RectF): Boolean =
    rectF.contains(x, y)


fun CropEdges.asRectF(width: Int): RectF =
    RectF(0F, top.toFloat(), width.toFloat(), bottom.toFloat())