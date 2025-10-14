package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.Matrix
import android.graphics.RectF
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.mapRect
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.maxRectOf
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.minRectOf
import com.w2sv.autocrop.ui.screen.cropadjustment.model.Edge
import kotlin.math.max

data class DragLimits(val min: RectF, val max: RectF) {

    fun applyTo(rect: RectF) {
        rect.set(maxRectOf(rect, max))
        rect.set(minRectOf(rect, min))
    }

    class Factory(
        private val draggedEdge: Edge,
        private val cropRect: RectF,
        private val imageMatrix: Matrix,
        private val imageRect: RectF,
        private val viewRect: RectF,
        private val bitmapMaxScale: Float = BITMAP_MAX_SCALE,
        private val minRectSize: Float = MIN_RECT_SIZE
    ) {
        init {
            require(imageRect.width() > 0 && imageRect.height() > 0) {
                "imageRect must be initialized before creating DragLimits"
            }
        }

        fun compute(): DragLimits =
            DragLimits(min(), max())

        private fun min(): RectF {
            val imageMinRect = (max(imageRect.width(), imageRect.height()) / bitmapMaxScale).let { bitmapMinRectSize ->
                RectF(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)
            }
            val minSize = max(
                mapRect(imageMinRect, RectF(), imageMatrix).width(),
                minRectSize
            )

            return when (draggedEdge) {
                Edge.TOP -> RectF(
                    cropRect.left,
                    cropRect.bottom - minSize,
                    cropRect.right,
                    cropRect.bottom
                )

                Edge.BOTTOM -> RectF(
                    cropRect.left,
                    cropRect.top,
                    cropRect.right,
                    cropRect.top + minSize
                )
            }
        }

        private fun max(): RectF {
            val borderRect = maxRectOf(imageRect, viewRect)

            return when (draggedEdge) {
                Edge.TOP -> RectF(
                    cropRect.left,
                    borderRect.top,
                    cropRect.right,
                    cropRect.bottom
                )

                Edge.BOTTOM -> RectF(
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
}
