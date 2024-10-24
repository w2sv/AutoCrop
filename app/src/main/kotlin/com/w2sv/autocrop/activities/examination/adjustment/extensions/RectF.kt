package com.w2sv.autocrop.activities.examination.adjustment.extensions

import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import com.w2sv.autocrop.activities.examination.adjustment.model.Edge
import com.w2sv.cropbundle.cropping.model.CropEdges
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

fun RectF.contains(x: Float, y: Float, toleranceMargin: Float = 0f): Boolean =
    x >= left - toleranceMargin && x < right + toleranceMargin && y >= top - toleranceMargin && y < bottom + toleranceMargin

fun RectF.containsVerticalEdges(y1: Float, y2: Float): Boolean =
    y1 > top && y2 < bottom

fun RectF.setVerticalEdges(y1: Float, y2: Float) {
    top = y1
    bottom = y2
}

fun RectF.getCopy(): RectF =
    RectF(this)

fun RectF.asMappedFrom(src: RectF, mapMatrix: Matrix): RectF =
    apply {
        mapMatrix.mapRect(
            this,
            src
        )
    }

fun CropEdges.asRectF(width: Int): RectF =
    RectF(0F, top.toFloat(), width.toFloat(), bottom.toFloat())