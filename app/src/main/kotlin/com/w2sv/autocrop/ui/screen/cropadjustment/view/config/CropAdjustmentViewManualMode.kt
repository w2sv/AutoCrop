package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.mapRect
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.domain.model.CropEdges
import com.w2sv.kotlinutils.threadUnsafeLazy

class CropAdjustmentViewManualMode(private val view: CropAdjustmentView, context: Context) : CropAdjustmentViewMode {

    private val animator = CropAnimator(view)
    private val gridDrawer = CropGridDrawer(context)

    private val dragHandler by threadUnsafeLazy {
        DragHandler(
            view = view,
            onDrag = { view.emitModeState(AdjustmentModeState.Manual(view.remappedCropEdges())) },
            onDragEnded = { animator.centerCropRect() }
        )
    }

    override fun onDraw(canvas: Canvas) {
        view.drawCropMask(canvas)
        gridDrawer.draw(canvas, view.cropRect)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        dragHandler.onTouchEvent(event)

    override fun updateFromEdges(edges: CropEdges) {
        animator.animateTo(
            dstMatrix = view.defaultTransformationMatrix,
            dstCropRect = mapRect(src = view.cropEdgesRect, matrix = view.defaultTransformationMatrix)
        )
    }
}
