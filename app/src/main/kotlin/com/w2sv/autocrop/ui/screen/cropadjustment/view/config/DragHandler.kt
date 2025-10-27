package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.contains
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.getEdgeTouch
import com.w2sv.autocrop.ui.screen.cropadjustment.model.Edge
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.autocrop.ui.screen.cropadjustment.view.ViewSpaceRect
import com.w2sv.kotlinutils.threadUnsafeLazy

class DragHandler(
    private val view: ImageView,
    private val cropRect: () -> ViewSpaceRect,
    private val imageRect: () -> ViewSpaceRect,
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
                    var top = cropRect().top - distanceY
                    var bottom = top + cropRect().height()

                    // Clamp vertical bounds
                    when {
                        top < imageRect().top -> {
                            top = imageRect().top
                            bottom = top + cropRect().height()
                        }

                        bottom > imageRect().bottom -> {
                            bottom = imageRect().bottom
                            top = bottom - cropRect().height()
                        }
                    }

                    cropRect().apply {
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
        val edge = cropRect().getEdgeTouch(event, CropAdjustmentView.VERTICAL_EDGE_TOUCH_SLOP.toFloat())

        state = when {
            edge != null -> DragState.DraggingEdge(edge, view, cropRect, imageRect)
            cropRect().contains(event) -> DragState.DraggingCropRect.also {
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
                        Edge.TOP -> cropRect().top = event.y
                        Edge.BOTTOM -> cropRect().bottom = event.y
                    }
                    state.dragLimits.applyTo(cropRect())
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
            constructor(edge: Edge, view: ImageView, cropRect: () -> ViewSpaceRect, imageRect: () -> ViewSpaceRect) : this(
                edge = edge,
                dragLimits = DragLimits.Factory(
                    draggedEdge = edge,
                    cropRect = cropRect(),
                    imageMatrix = view.imageMatrix,
                    imageRect = imageRect(),
                    viewRect = RectF(0f, 0f, view.width.toFloat(), view.height.toFloat())
                )
                    .compute()
            )
        }

        data object DraggingCropRect : DragState
    }
}
