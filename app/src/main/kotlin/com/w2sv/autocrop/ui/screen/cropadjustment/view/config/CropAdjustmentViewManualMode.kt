package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.kotlinutils.threadUnsafeLazy

class CropAdjustmentViewManualMode(private val view: CropAdjustmentView, context: Context) : CropAdjustmentViewMode {

    private val animator by threadUnsafeLazy { CropAnimator(view) }

    private val gridDrawer = CropGridDrawer(context, innerGridAlpha = { animator.gridAlpha })

    private val dragHandler by threadUnsafeLazy {
        DragHandler(
            view = view,
            onDragStateChanged = { view.emitModeState(AdjustmentModeState.Manual(view.remappedCropEdges())) },
            onDragStarted = { animator.resetGridAlpha() },
            onDragEnded = { animator.animateToCenter() }
        )
    }

    override fun setUp() {
        view.resetCropRect()
        view.invalidate()
    }

    override fun reset() {
        animator.animateToInitialConfig()
    }

    override fun onDraw(canvas: Canvas) {
        view.drawCropMask(canvas)
        gridDrawer.draw(canvas, view.cropRect)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        dragHandler.onTouchEvent(event)
}
