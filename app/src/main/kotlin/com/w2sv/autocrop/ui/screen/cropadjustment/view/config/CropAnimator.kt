package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.Matrix
import android.graphics.RectF
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.animateTo
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.autocrop.ui.util.view.getCopy
import com.w2sv.common.util.log
import kotlin.math.min

class CropAnimator(private val view: CropAdjustmentView) {

    fun animateToCenter() {
        val centerRect = centeredCropRect()
        animateImageTo(centerRect)
        animateCropRectTo(centerRect)
        view.invalidate()
    }

    fun animateCropRectTo(dst: RectF) {
        view.cropRect.animateTo(dst, CropAdjustmentView.ANIMATION_DURATION) {
            view.invalidate()
        }
    }

    private fun centeredCropRect(): RectF {
        val width = view.cropRect.width()
        val height = view.cropRect.height()

        val left = (view.width.toFloat() - width) / 2f
        val top = (view.height.toFloat() - height) / 2f
        val right = left + width
        val bottom = top + height

        return RectF(left, top, right, bottom)
    }

    private fun animateImageTo(dst: RectF) {
        val newBitmapMatrix = view.imageMatrix.getCopy()

        val scale = dst.width() / view.cropRect.width()
        val translateX = dst.centerX() - view.cropRect.centerX()
        val translateY = dst.centerY() - view.cropRect.centerY()

        val matrix = Matrix().apply {
            setScale(scale, scale, view.cropRect.centerX(), view.cropRect.centerY())
            postTranslate(translateX, translateY)
        }
        newBitmapMatrix.postConcat(matrix)
        view.animateImageTo(newBitmapMatrix)
    }
}
