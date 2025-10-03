package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.Matrix
import android.graphics.RectF
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.mapRect
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.maxRectOf
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.minRectOf
import com.w2sv.autocrop.ui.screen.cropadjustment.model.Edge
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlin.math.max

data class DragLimits(
    val min: RectF = RectF(),
    val max: RectF = RectF(),
    private val imageRect: RectF,
    private val viewRectProvider: () -> RectF,
    private val bitmapMaxScale: Float = BITMAP_MAX_SCALE,
    private val minRectSize: Float = MIN_RECT_SIZE
) {
    init {
        require(imageRect.width() > 0 && imageRect.height() > 0) {
            "imageRect must be initialized before creating DragLimits"
        }
    }

    private val imageMinRect by threadUnsafeLazy {
        val bitmapMinRectSize = max(imageRect.width(), imageRect.height()) / bitmapMaxScale
        RectF(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)
    }

    fun setEmpty() {
        min.setEmpty()
        max.setEmpty()
    }

    fun compute(draggedEdge: Edge, cropRect: RectF, imageMatrix: Matrix, imageBorderRect: RectF) {
        computeMin(draggedEdge, cropRect, imageMatrix)
        computeMax(draggedEdge, cropRect, imageBorderRect)
    }

    fun applyTo(rect: RectF) {
        rect.set(maxRectOf(rect, max))
        rect.set(minRectOf(rect, min))
    }

    private fun computeMin(draggedEdge: Edge, cropRect: RectF, imageMatrix: Matrix) {
        val minSize = max(
            mapRect(imageMinRect, RectF(), imageMatrix).width(),
            minRectSize
        )

        when (draggedEdge) {
            Edge.TOP -> min.set(
                cropRect.left,
                cropRect.bottom - minSize,
                cropRect.right,
                cropRect.bottom
            )

            Edge.BOTTOM -> min.set(
                cropRect.left,
                cropRect.top,
                cropRect.right,
                cropRect.top + minSize
            )
        }
    }

    private fun computeMax(draggedEdge: Edge, cropRect: RectF, imageBorderRect: RectF) {
        val borderRect = maxRectOf(imageBorderRect, viewRectProvider())

        when (draggedEdge) {
            Edge.TOP -> max.set(
                cropRect.left,
                borderRect.top,
                cropRect.right,
                cropRect.bottom
            )

            Edge.BOTTOM -> max.set(
                cropRect.left,
                cropRect.top,
                cropRect.right,
                borderRect.bottom
            )
        }
    }

    companion object {
        private const val BITMAP_MAX_SCALE = 15f
        private const val MIN_RECT_SIZE: Float = 56f
    }
}
