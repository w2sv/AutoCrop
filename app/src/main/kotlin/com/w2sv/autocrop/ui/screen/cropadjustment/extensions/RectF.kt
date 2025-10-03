package com.w2sv.autocrop.ui.screen.cropadjustment.extensions

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import com.w2sv.autocrop.ui.screen.cropadjustment.model.Edge
import com.w2sv.domain.model.CropEdges
import java.lang.Float.min
import kotlin.math.max

fun RectF.animateTo(
    target: RectF,
    duration: Long = 300L,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    onUpdate: (RectF) -> Unit
): ValueAnimator {
    val startLeft = left
    val startTop = top
    val startRight = right
    val startBottom = bottom

    return ValueAnimator.ofFloat(0f, 1f).apply {
        this.interpolator = interpolator
        this.duration = duration
        addUpdateListener { animator ->
            val fraction = animator.animatedFraction

            left = startLeft + (target.left - startLeft) * fraction
            top = startTop + (target.top - startTop) * fraction
            right = startRight + (target.right - startRight) * fraction
            bottom = startBottom + (target.bottom - startBottom) * fraction

            onUpdate(this@animateTo)
        }
        start()
    }
}

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

fun RectF.getEdgeTouch(touchEvent: MotionEvent, touchThreshold: Float): Edge? =
    when {
        touchEvent.x > right || touchEvent.x < left -> null

        touchEvent.y < top + touchThreshold &&
            touchEvent.y > top - touchThreshold -> Edge.TOP

        touchEvent.y < bottom + touchThreshold &&
            touchEvent.y > bottom - touchThreshold -> Edge.BOTTOM

        else -> null
    }

fun RectF.contains(event: MotionEvent, toleranceMargin: Float = 0f): Boolean =
    contains(event.x, event.y, toleranceMargin)

fun RectF.contains(
    x: Float,
    y: Float,
    toleranceMargin: Float = 0f
): Boolean =
    x >= left - toleranceMargin && x < right + toleranceMargin && y >= top - toleranceMargin && y < bottom + toleranceMargin

fun RectF.containsVerticalEdges(y1: Float, y2: Float): Boolean =
    y1 > top && y2 < bottom

fun mapRect(src: RectF, dst: RectF, matrix: Matrix): RectF {
    matrix.mapRect(dst, src)
    return dst
}

fun CropEdges.rectF(width: Int): RectF =
    RectF(0F, top.toFloat(), width.toFloat(), bottom.toFloat())
