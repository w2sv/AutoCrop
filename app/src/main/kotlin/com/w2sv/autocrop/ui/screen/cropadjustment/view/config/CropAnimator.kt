package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.RectF
import android.view.animation.LinearInterpolator
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.animateTo
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.autocrop.ui.util.view.animateMatrix

private const val ALPHA_MAX = 255

class CropAnimator(private val view: CropAdjustmentView) {

    var gridAlpha = 0
        private set

    private val gridFadeOutAnimator = ValueAnimator.ofInt(ALPHA_MAX, 0).apply {
        startDelay = 750
        duration = ANIMATION_DURATION
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

    fun centerCropRect() {
        val viewCenteredCropRect = view.cropRect.centeredAcross(view.width.toFloat(), view.height.toFloat())
        val dstMatrix = rectToRectMappingMatrix(src = view.cropRect, dst = viewCenteredCropRect, srcMatrix = view.transformationMatrix)
        animateTo(dstMatrix, viewCenteredCropRect)
        gridFadeOutAnimator.start()
    }

    fun animateTo(dstMatrix: Matrix, dstCropRect: RectF) {
        animateImageTo(dstMatrix)
        animateCropRectTo(dstCropRect)
    }

    private fun animateImageTo(dst: Matrix) {
        animateMatrix(
            src = view.transformationMatrix,
            dst = dst,
            duration = ANIMATION_DURATION,
            onUpdate = { matrix ->
                view.transformationMatrix = matrix
                view.invalidate()
            }
        )
    }

    private fun animateCropRectTo(dst: RectF) {
        view.cropRect.animateTo(
            target = dst,
            duration = ANIMATION_DURATION,
            onUpdate = { view.invalidate() }
        )
    }

    companion object {
        private const val ANIMATION_DURATION: Long = 300
    }
}

private fun RectF.centeredAcross(referenceWidth: Float, referenceHeight: Float): RectF {
    val left = (referenceWidth - width()) / 2f
    val top = (referenceHeight - height()) / 2f
    val right = left + width()
    val bottom = top + height()

    return RectF(left, top, right, bottom)
}

/**
 * Computes a transformation matrix that maps the [src] rectangle to the [dst] rectangle,
 * applying the resulting scale and translation on top of the given [srcMatrix].
 *
 * The returned matrix preserves the existing transformations of [srcMatrix] and
 * adds the delta required to align [src] to [dst].
 *
 * @param src The source rectangle to transform.
 * @param dst The target rectangle to map [src] onto.
 * @param srcMatrix The base matrix whose transformations should be preserved.
 * @return A new [Matrix] representing [srcMatrix] combined with the delta transform
 *         that aligns [src] with [dst].
 */
private fun rectToRectMappingMatrix(
    src: RectF,
    dst: RectF,
    srcMatrix: Matrix
): Matrix =
    Matrix(srcMatrix).apply {
        val scale = dst.width() / src.width()
        val translateX = dst.centerX() - src.centerX()
        val translateY = dst.centerY() - src.centerY()

        postScale(scale, scale, src.centerX(), src.centerY())
        postTranslate(translateX, translateY)
    }
