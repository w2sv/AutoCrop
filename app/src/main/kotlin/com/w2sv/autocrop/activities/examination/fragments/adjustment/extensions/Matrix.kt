package com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions

import android.animation.ObjectAnimator
import android.graphics.Matrix
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.animation.MatrixEvaluator

fun animateMatrix(view: View, propertyName: String, src: Matrix, dst: Matrix, duration: Long) {
    ObjectAnimator.ofObject(view, propertyName, MatrixEvaluator(), src, dst)
        .apply {
            interpolator = AccelerateDecelerateInterpolator()
            this.duration = duration
            addUpdateListener {
                view.invalidate()
            }
        }
        .start()
}

fun Matrix.getScaleX(): Float =
    getValues()[Matrix.MSCALE_X]

fun Matrix.getScaleY(): Float =
    getValues()[Matrix.MSCALE_Y]

fun Matrix.getTranslateX(): Float =
    getValues()[Matrix.MTRANS_X]

fun Matrix.getTranslateY(): Float =
    getValues()[Matrix.MTRANS_Y]

fun Matrix.clone(): Matrix =
    Matrix().apply {
        set(this@clone)
    }

fun Matrix.getInverse(): Matrix {
    val inverse = Matrix()
    invert(inverse)
    return inverse
}

private fun Matrix.getValues(): FloatArray =
    FloatArray(9).apply {
        getValues(this)
    }