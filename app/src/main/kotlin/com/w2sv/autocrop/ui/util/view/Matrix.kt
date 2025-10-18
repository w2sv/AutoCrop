package com.w2sv.autocrop.ui.util.view

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.animation.MatrixEvaluator

fun animateMatrix(
    src: Matrix,
    dst: Matrix,
    duration: Long,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    configure: Animator.() -> Unit = {},
    onUpdate: (Matrix) -> Unit
): ValueAnimator =
    ValueAnimator.ofObject(MatrixEvaluator(), src, dst).apply {
        this.duration = duration
        this.interpolator = interpolator
        addUpdateListener { animator ->
            onUpdate(animator.animatedValue as Matrix)
        }
        configure()
        start()
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

private fun Matrix.getValues(): FloatArray =
    FloatArray(9).apply {
        getValues(this)
    }
