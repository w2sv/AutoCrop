package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlin.getValue

class CropGridDrawer(private val context: Context) {

    private val gridPaint by threadUnsafeLazyPaint {
        color = Color.WHITE
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val horizontalCropRectEdgePaint by threadUnsafeLazyPaint {
        color = context.getColor(com.w2sv.core.common.R.color.magenta_saturated)
        strokeWidth = 3f
        style = Paint.Style.FILL
    }

    private val horizontalProtrusionPaint by threadUnsafeLazyPaint {
        color = context.getColor(com.w2sv.core.common.R.color.magenta_saturated)
        strokeWidth = 8f
        style = Paint.Style.FILL
    }

    fun draw(canvas: Canvas, cropRect: RectF) {
        drawOuterRectangle(canvas, cropRect)
        drawProtrusions(canvas, cropRect)
        drawInnerGrid(canvas, cropRect)
    }

    private fun drawOuterRectangle(canvas: Canvas, rect: RectF) {
        // Vertical edges
        canvas.drawLine(rect.left, rect.bottom, rect.left, rect.top, gridPaint)
        canvas.drawLine(rect.right, rect.bottom, rect.right, rect.top, gridPaint)

        // Horizontal edges
        canvas.drawLine(rect.left, rect.top, rect.right, rect.top, horizontalCropRectEdgePaint)
        canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, horizontalCropRectEdgePaint)
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

private fun threadUnsafeLazyPaint(block: Paint.() -> Unit) =
    threadUnsafeLazy { Paint().apply(block) }
