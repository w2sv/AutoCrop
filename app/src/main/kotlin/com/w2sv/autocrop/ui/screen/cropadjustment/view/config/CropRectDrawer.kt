package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.w2sv.autocrop.ui.screen.cropadjustment.view.ViewSpaceRect
import com.w2sv.autocrop.ui.util.view.threadUnsafeLazyPaint

class CropRectDrawer(private val context: Context) {

    private val paint by threadUnsafeLazyPaint {
        color = context.getColor(com.w2sv.core.common.R.color.magenta_saturated)
        strokeWidth = 3f
        style = Paint.Style.FILL
    }

    fun draw(canvas: Canvas, cropRect: ViewSpaceRect) {
        drawHorizontalEdges(canvas, cropRect)
        drawProtrusions(canvas, cropRect)
    }

    private fun drawHorizontalEdges(canvas: Canvas, rect: RectF) {
        canvas.drawLine(rect.left, rect.top, rect.right, rect.top, paint)
        canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, paint)
    }

    private fun drawProtrusions(canvas: Canvas, rect: RectF) {
        // top
        canvas.drawRoundRect(
            rect.centerX() - PROTRUSION_WIDTH / 2f,
            rect.top - PROTRUSION_HEIGHT / 2f,
            rect.centerX() + PROTRUSION_WIDTH / 2f,
            rect.top + PROTRUSION_HEIGHT / 2f,
            PROTRUSION_CORNER_RADIUS,
            PROTRUSION_CORNER_RADIUS,
            paint
        )

        // bottom
        canvas.drawRoundRect(
            rect.centerX() - PROTRUSION_WIDTH / 2f,
            rect.bottom - PROTRUSION_HEIGHT / 2f,
            rect.centerX() + PROTRUSION_WIDTH / 2f,
            rect.bottom + PROTRUSION_HEIGHT / 2f,
            PROTRUSION_CORNER_RADIUS,
            PROTRUSION_CORNER_RADIUS,
            paint
        )
    }

    companion object {
        private const val PROTRUSION_WIDTH = 152f
        private const val PROTRUSION_HEIGHT = 14f
        private const val PROTRUSION_CORNER_RADIUS = PROTRUSION_HEIGHT / 2
    }
}
