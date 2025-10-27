package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.Canvas
import android.graphics.RectF
import android.view.MotionEvent
import android.widget.ImageView
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.mapRect
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.kotlinutils.threadUnsafeLazy

class CropAdjustmentViewManualMode(
    private val view: ImageView,
    private val cropState: CropAdjustmentView.CropState,
    private val imageMatrixController: CropAdjustmentView.ImageMatrixController,
    private val emitModeState: (AdjustmentModeState) -> Unit
) : CropAdjustmentViewMode {

    private val animator = CropAnimator(view, cropRect = { cropState.cropRect })
    private val gridDrawer = CropRectDrawer(view.context)

    private val dragHandler by threadUnsafeLazy {
        DragHandler(
            view = view,
            cropRect = { cropState.cropRect },
            imageRect = { imageMatrixController.imageRect },
            onDrag = { emitModeState(AdjustmentModeState.Manual(cropState.bitmapSpaceRemappedCropEdges())) },
            onDragEnded = { animator.centerCropRect() }
        )
    }

    override fun onDraw(canvas: Canvas) {
        gridDrawer.draw(canvas, cropState.cropRect)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        dragHandler.onTouchEvent(event)

    override fun displayCropRect(dstRect: RectF) {
        val dstMatrix = imageMatrixController.centerFitMatrix
        val dstCropRect = mapRect(src = dstRect, matrix = dstMatrix)
        animator.animateTo(
            dstMatrix = dstMatrix,
            dstCropRect = dstCropRect
        )
    }
}
