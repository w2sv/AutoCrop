package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.w2sv.autocrop.ui.util.view.buildPaint
import com.w2sv.autocrop.ui.util.view.threadUnsafeLazyPaint

class CropGridDrawer(private val context: Context, private val innerGridAlpha: () -> Int) {

    private val gridPaint get() = buildPaint {
        color = Color.WHITE
        strokeWidth = 2f
        style = Paint.Style.STROKE
        alpha = innerGridAlpha()
    }

    private val cropRectEdgePaint by threadUnsafeLazyPaint {
        color = context.getColor(com.w2sv.core.common.R.color.magenta_saturated)
        strokeWidth = 7f
        style = Paint.Style.FILL
    }

    private val horizontalProtrusionPaint by threadUnsafeLazyPaint {
        color = context.getColor(com.w2sv.core.common.R.color.magenta_saturated)
        strokeWidth = 14f
        style = Paint.Style.FILL
    }

    fun draw(canvas: Canvas, cropRect: RectF) {
        drawInnerGrid(canvas, cropRect)
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

    private fun drawInnerGrid(canvas: Canvas, rect: RectF) {
        val thirdWidth = rect.width() / 3f
        val thirdHeight = rect.height() / 3f

        // Vertical lines
        canvas.drawLine(rect.left + thirdWidth, rect.top, rect.left + thirdWidth, rect.bottom, gridPaint)
        canvas.drawLine(rect.left + 2f * thirdWidth, rect.top, rect.left + 2f * thirdWidth, rect.bottom, gridPaint)

        // Horizontal lines
        canvas.drawLine(rect.left, rect.top + thirdHeight, rect.right, rect.top + thirdHeight, gridPaint)
        canvas.drawLine(rect.left, rect.top + 2f * thirdHeight, rect.right, rect.top + 2f * thirdHeight, gridPaint)
    }

    companion object {
        private const val DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION = 32f
    }
}
