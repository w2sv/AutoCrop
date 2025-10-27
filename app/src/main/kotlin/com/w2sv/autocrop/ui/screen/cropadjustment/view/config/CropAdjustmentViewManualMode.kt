package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.Canvas
import android.view.MotionEvent
import android.widget.ImageView
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.BitmapSpaceRect
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.autocrop.ui.screen.cropadjustment.view.viewSpace
import com.w2sv.autocrop.ui.util.view.mappedRect
import com.w2sv.kotlinutils.threadUnsafeLazy

class CropAdjustmentViewManualMode(
    private val view: ImageView,
    private val cropState: CropAdjustmentView.CropState,
    private val coordinateMapper: CropAdjustmentView.ImageCoordinateMapper,
    private val emitModeState: (AdjustmentModeState) -> Unit
) : CropAdjustmentViewMode {

    private val animator = CropAnimator(view, cropRect = { cropState.cropRect })
    private val gridDrawer = CropRectDrawer(view.context)

    private val dragHandler by threadUnsafeLazy {
        DragHandler(
            view = view,
            cropRect = { cropState.cropRect },
            imageRect = { coordinateMapper.imageRect },
            onDrag = { emitModeState(AdjustmentModeState.Manual(cropState.remappedCropEdges())) },
            onDragEnded = { animator.centerCropRect() }
        )
    }

    override fun onDraw(canvas: Canvas) {
        gridDrawer.draw(canvas, cropState.cropRect)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        dragHandler.onTouchEvent(event)

    override fun displayCropRect(rect: BitmapSpaceRect) {
        val dstMatrix = coordinateMapper.centerFitMatrix
        val dstCropRect = dstMatrix.mappedRect(rect).viewSpace
        animator.animateTo(
            dstMatrix = dstMatrix,
            dstCropRect = dstCropRect
        )
    }
}
