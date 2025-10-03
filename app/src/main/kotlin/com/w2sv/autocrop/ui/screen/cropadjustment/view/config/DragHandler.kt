package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.contains
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.getEdgeTouch
import com.w2sv.autocrop.ui.screen.cropadjustment.model.Edge
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlin.math.max
import kotlin.math.min

class DragHandler(
    private val view: CropAdjustmentView,
    private val onStateChanged: () -> Unit,
    private val onDragStarted: () -> Unit,
    private val onDragEnded: () -> Unit
) {
    private var state: DraggingState? = null
    private val dragLimits by threadUnsafeLazy {
        DragLimits(
            imageRect = view.imageRectBitmapSpace,
            viewRectProvider = { RectF(0f, 0f, view.width.toFloat(), view.height.toFloat()) }
        )
    }

    private val gestureDetector by threadUnsafeLazy {
        GestureDetector(
            view.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    var top = view.cropRect.top - distanceY
                    var bottom = top + view.cropRect.height()

                    // Clamp vertical bounds
                    when {
                        top < view.imageRect.top -> {
                            top = view.imageRect.top
                            bottom = top + view.cropRect.height()
                        }
                        bottom > view.imageRect.bottom -> {
                            bottom = view.imageRect.bottom
                            top = bottom - view.cropRect.height()
                        }
                    }

                    view.cropRect.apply {
                        this.top = top
                        this.bottom = bottom
                    }
                    return true
                }
            }
        )
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP -> onActionUp()
        }
        return true
    }

    private fun onActionDown(event: MotionEvent) {
        val edge = view.cropRect.getEdgeTouch(event, TOUCH_TOLERANCE_MARGIN)

        state = when {
            edge != null -> DraggingState.DraggingEdge(edge).also {
                dragLimits.compute(
                    draggedEdge = it.edge,
                    cropRect = view.cropRect,
                    imageMatrix = view.transformationMatrix,
                    imageBorderRect = view.imageRect
                )
            }

            view.cropRect.contains(event) -> DraggingState.DraggingCropRect.also {
                gestureDetector.onTouchEvent(event)
            }

            else -> null
        }

        state?.run { onDragStarted() }
    }

    private fun onActionMove(event: MotionEvent) {
        state?.let { state ->
            when (state) {
                is DraggingState.DraggingEdge -> {
                    when (state.edge) {
                        Edge.TOP -> view.cropRect.top = event.y
                        Edge.BOTTOM -> view.cropRect.bottom = event.y
                    }
                    dragLimits.applyTo(view.cropRect)
                }

                is DraggingState.DraggingCropRect -> {
                    gestureDetector.onTouchEvent(event)
                }
            }

            onStateChanged()
            view.invalidate()
        }
    }

    private fun onActionUp() {
        if (state == null) return
        dragLimits.setEmpty()
        state = null
        onDragEnded()
    }

    private sealed interface DraggingState {
        @JvmInline
        value class DraggingEdge(val edge: Edge) : DraggingState
        data object DraggingCropRect : DraggingState
    }

    companion object {
        const val TOUCH_TOLERANCE_MARGIN: Float = 42f
    }
}
