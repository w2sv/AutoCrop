package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.contains
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.getEdgeTouch
import com.w2sv.autocrop.ui.screen.cropadjustment.model.Edge
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.kotlinutils.threadUnsafeLazy

class DragHandler(
    private val view: CropAdjustmentView,
    private val onDragStarted: () -> Unit = {},
    private val onDrag: () -> Unit = {},
    private val onDragEnded: () -> Unit = {}
) {
    private var state: DragState? = null

    /**
     * Handles [DragState.DraggingCropRect].
     */
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

    /**
     * @return True if the [event] was handled, false otherwise.
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onActionUp()
            else -> return false
        }
        return true
    }

    private fun onActionDown(event: MotionEvent) {
        val edge = view.cropRect.getEdgeTouch(event, CropAdjustmentView.VERTICAL_EDGE_TOUCH_SLOP.toFloat())

        state = when {
            edge != null -> DragState.DraggingEdge(edge, view)
            view.cropRect.contains(event) -> DragState.DraggingCropRect.also {
                gestureDetector.onTouchEvent(event)
            }

            else -> return
        }
        onDragStarted()
    }

    private fun onActionMove(event: MotionEvent) {
        state?.let { state ->
            when (state) {
                is DragState.DraggingEdge -> {
                    when (state.edge) {
                        Edge.TOP -> view.cropRect.top = event.y
                        Edge.BOTTOM -> view.cropRect.bottom = event.y
                    }
                    state.dragLimits.applyTo(view.cropRect)
                }

                is DragState.DraggingCropRect -> {
                    gestureDetector.onTouchEvent(event)
                }
            }

            onDrag()
            view.invalidate()
        }
    }

    private fun onActionUp() {
        if (state == null) return
        state = null
        onDragEnded()
    }

    private sealed interface DragState {
        data class DraggingEdge(val edge: Edge, val dragLimits: DragLimits) : DragState {
            constructor(edge: Edge, view: CropAdjustmentView) : this(
                edge = edge,
                dragLimits = DragLimits.Factory(
                    draggedEdge = edge,
                    cropRect = view.cropRect,
                    imageMatrix = view.imageMatrix,
                    imageRect = view.imageRect,
                    viewRect = RectF(0f, 0f, view.width.toFloat(), view.height.toFloat())
                )
                    .compute()
            )
        }

        data object DraggingCropRect : DragState
    }
}
