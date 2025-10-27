package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.graphics.Matrix
import android.graphics.RectF
import android.widget.ImageView
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.animateTo
import com.w2sv.autocrop.ui.screen.cropadjustment.view.ViewSpaceRect
import com.w2sv.autocrop.ui.screen.cropadjustment.view.viewSpace
import com.w2sv.autocrop.ui.util.view.animateTo

class CropAnimator(private val view: ImageView, private val cropRect: () -> ViewSpaceRect) {

    fun centerCropRect() {
        val viewCenteredCropRect = cropRect().centeredAcross(view.width.toFloat(), view.height.toFloat()).viewSpace
        val dstMatrix = rectToRectMappingMatrix(src = cropRect(), dst = viewCenteredCropRect, srcMatrix = view.imageMatrix)
        animateTo(dstMatrix, viewCenteredCropRect)
    }

    fun animateTo(dstMatrix: Matrix, dstCropRect: ViewSpaceRect) {
        animateCropRectTo(dstCropRect)
        animateImageTo(dstMatrix)
    }

    private fun animateImageTo(dst: Matrix) {
        view.imageMatrix.animateTo(
            dst = dst,
            duration = ANIMATION_DURATION,
            onUpdate = { matrix ->
                // view.invalidate() automatically called when setting imageMatrix
                view.imageMatrix = matrix
            }
        )
    }

    private fun animateCropRectTo(dst: ViewSpaceRect) {
        cropRect().animateTo(
            dst = dst,
            duration = ANIMATION_DURATION,
            onUpdate = { rect ->
                cropRect().set(rect.viewSpace)
                view.invalidate()
            }
        )
    }

    companion object {
        private const val ANIMATION_DURATION: Long = 500
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
