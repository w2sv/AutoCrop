package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.kotlinutils.threadUnsafeLazy

class CropAdjustmentViewManualMode(private val view: CropAdjustmentView, context: Context) : CropAdjustmentViewMode {

    private val gridDrawer = CropGridDrawer(context)

    private val animator by threadUnsafeLazy { CropAnimator(view) }

    private val dragHandler by threadUnsafeLazy {
        DragHandler(
            view = view,
            onStateChanged = { view.emitModeState(AdjustmentModeState.Manual(view.remappedCropEdges())) },
            onDragEnded = { animator.animateToCenter() }
        )
    }

    override fun setUp() {
        view.setImageBorderRect()
        view.resetCropRect()
        view.invalidate()
    }

    override fun reset() {
        view.animateImageTo(view.defaultImageMatrix)
        animator.animateCropRectTo(view.defaultCropRect)
        view.setImageBorderRect()
    }

    override fun onDraw(canvas: Canvas) {
        view.drawCropRect(canvas)
        gridDrawer.draw(canvas, view.cropRect)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        dragHandler.onTouchEvent(event)
}
