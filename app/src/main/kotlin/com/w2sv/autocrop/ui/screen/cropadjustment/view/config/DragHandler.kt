package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.contains
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.containsVerticalEdges
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.getEdgeTouch
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.setVerticalEdges
import com.w2sv.autocrop.ui.screen.cropadjustment.model.Edge
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.kotlinutils.threadUnsafeLazy

class DragHandler(
    private val view: CropAdjustmentView,
    private val onStateChanged: () -> Unit,
    private val onDragEnded: () -> Unit
) {
    private var state: DraggingState = DraggingState.Idle
    private val dragLimits by threadUnsafeLazy {
        DragLimits(
            imageRect = view.imageRect,
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
                    val y1 = view.cropRect.top - distanceY
                    val y2 = view.cropRect.bottom - distanceY

                    if (view.imageBorderRect.containsVerticalEdges(y1, y2)) {
                        view.cropRect.setVerticalEdges(y1, y2)
                        onStateChanged()
                        return true
                    }
                    return false
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

        if (state == DraggingState.DraggingCropRect) {
            gestureDetector.onTouchEvent(event)
            view.invalidate()
        }

        return true
    }

    private fun onActionDown(event: MotionEvent) {
        val edge = view.cropRect.getEdgeTouch(event, CropAdjustmentView.Companion.TOUCH_TOLERANCE_MARGIN)

        state = when {
            edge != null -> DraggingState.DraggingEdge(edge)
            view.cropRect.contains(event) -> DraggingState.DraggingCropRect
            else -> DraggingState.Idle
        }

        if (state !is DraggingState.Idle) view.setImageBorderRect()

        (state as? DraggingState.DraggingEdge)?.let {
            dragLimits.compute(
                draggedEdge = it.edge,
                cropRect = view.cropRect,
                imageMatrix = view.imageMatrix,
                imageBorderRect = view.imageBorderRect
            )
        }
    }

    private fun onActionMove(event: MotionEvent) {
        when (val state = state) {
            is DraggingState.DraggingEdge -> {
                when (state.edge) {
                    Edge.TOP -> view.cropRect.top = event.y
                    Edge.BOTTOM -> view.cropRect.bottom = event.y
                }
                dragLimits.applyTo(view.cropRect)
                onStateChanged()
                view.invalidate()
            }

            else -> Unit
        }
    }

    private fun onActionUp() {
        dragLimits.setEmpty()
        onDragEnded()
        state = DraggingState.Idle
    }

    private sealed interface DraggingState {
        @JvmInline
        value class DraggingEdge(val edge: Edge) : DraggingState
        data object DraggingCropRect : DraggingState
        data object Idle : DraggingState
    }
}
