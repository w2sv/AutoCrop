package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.RectF
import android.view.animation.LinearInterpolator
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.animateTo
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.autocrop.ui.util.view.animateMatrix
import com.w2sv.autocrop.ui.util.view.getCopy

private const val ALPHA_MAX = 255

class CropAnimator(private val view: CropAdjustmentView) {

    var gridAlpha = 0
        private set

    private val gridFadeOutAnimator = ValueAnimator.ofInt(ALPHA_MAX, 0).apply {
        startDelay = 750
        duration = 500
        interpolator = LinearInterpolator()
        addUpdateListener { animator ->
            gridAlpha = (animator.animatedValue as Int)
            view.invalidate() // Redraw with new alpha
        }
    }

    fun resetGridAlpha() {
        gridFadeOutAnimator.cancel()
        gridAlpha = ALPHA_MAX
    }

    fun animateToCenter() {
        val centerRect = centeredCropRect()
        animateImageTo(centerRect)
        animateCropRectTo(centerRect)
        gridFadeOutAnimator.start()
    }

    fun animateToInitialConfig() {
        animateImageTo(view.initialTransformationMatrix)
        animateCropRectTo(view.initialCropRect)
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
        val newBitmapMatrix = view.transformationMatrix.getCopy()

        val scale = dst.width() / view.cropRect.width()
        val translateX = dst.centerX() - view.cropRect.centerX()
        val translateY = dst.centerY() - view.cropRect.centerY()

        val matrix = Matrix().apply {
            setScale(scale, scale, view.cropRect.centerX(), view.cropRect.centerY())
            postTranslate(translateX, translateY)
        }
        newBitmapMatrix.postConcat(matrix)
        animateImageTo(newBitmapMatrix)
    }

    private fun animateImageTo(dst: Matrix, onEnd: (() -> Unit) = {}) {
        animateMatrix(
            src = view.transformationMatrix,
            dst = dst,
            duration = ANIMATION_DURATION,
            onUpdate = { matrix ->
                view.transformationMatrix = matrix
                view.invalidate()
            },
            onEnd = onEnd
        )
    }

    private fun animateCropRectTo(dst: RectF) {
        view.cropRect.animateTo(dst, ANIMATION_DURATION) {
            view.invalidate()
        }
    }

    companion object {
        private const val ANIMATION_DURATION: Long = 300
    }
}
