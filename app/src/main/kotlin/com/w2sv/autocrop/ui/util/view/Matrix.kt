package com.w2sv.autocrop.ui.util.view

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.animation.MatrixEvaluator

fun Matrix.animateTo(
    dst: Matrix,
    duration: Long,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    configure: Animator.() -> Unit = {},
    onUpdate: (Matrix) -> Unit
): ValueAnimator =
    ValueAnimator.ofObject(MatrixEvaluator(), this, dst).apply {
        this.duration = duration
        this.interpolator = interpolator
        addUpdateListener { animator ->
            onUpdate(animator.animatedValue as Matrix)
        }
        configure()
        start()
    }

fun Matrix.mappedRect(src: RectF, dst: RectF = RectF()): RectF {
    mapRect(dst, src)
    return dst
}

fun Matrix.getScaleX(): Float =
    getValues()[Matrix.MSCALE_X]

fun Matrix.getScaleY(): Float =
    getValues()[Matrix.MSCALE_Y]

fun Matrix.getTranslateX(): Float =
    getValues()[Matrix.MTRANS_X]

fun Matrix.getTranslateY(): Float =
    getValues()[Matrix.MTRANS_Y]

fun Matrix.inverse(): Matrix {
    val inverse = Matrix()
    invert(inverse)
    return inverse
}

private fun Matrix.getValues(): FloatArray {
    val values = FloatArray(9)
    getValues(values)
    return values
}
