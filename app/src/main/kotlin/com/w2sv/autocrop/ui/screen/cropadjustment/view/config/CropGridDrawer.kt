package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.w2sv.autocrop.ui.util.view.threadUnsafeLazyPaint

class CropGridDrawer(private val context: Context) {

    private val cropRectEdgePaint by threadUnsafeLazyPaint {
        color = context.getColor(com.w2sv.core.common.R.color.magenta_saturated)
        strokeWidth = 3f
        style = Paint.Style.FILL
    }

    private val horizontalProtrusionPaint by threadUnsafeLazyPaint {
        color = context.getColor(com.w2sv.core.common.R.color.magenta_saturated)
        strokeWidth = 14f
        style = Paint.Style.FILL
    }

    fun draw(canvas: Canvas, cropRect: RectF) {
        drawHorizontalEdges(canvas, cropRect)
        drawProtrusions(canvas, cropRect)
    }

    private fun drawHorizontalEdges(canvas: Canvas, rect: RectF) {
        canvas.drawLine(rect.left, rect.top, rect.right, rect.top, cropRectEdgePaint)
        canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, cropRectEdgePaint)
    }

    private fun drawProtrusions(canvas: Canvas, rect: RectF) {
        // Top
        canvas.drawLine(
            rect.centerX() - DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            rect.top,
            rect.centerX() + DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            rect.top,
            horizontalProtrusionPaint
        )

        // Bottom
        canvas.drawLine(
            rect.centerX() - DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            rect.bottom,
            rect.centerX() + DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            rect.bottom,
            horizontalProtrusionPaint
        )
    }

    companion object {
        private const val DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION = 32f
    }
}
