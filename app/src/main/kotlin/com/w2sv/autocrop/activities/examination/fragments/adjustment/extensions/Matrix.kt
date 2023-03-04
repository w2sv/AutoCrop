package com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions

import android.animation.ObjectAnimator
import android.graphics.Matrix
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.google.android.material.animation.MatrixEvaluator

fun animateMatrix(
    view: View,
    propertyName: String,
    src: Matrix,
    dst: Matrix,
    duration: Long,
    onEnd: (() -> Unit)? = null
) {
    ObjectAnimator.ofObject(view, propertyName, MatrixEvaluator(), src, dst)
        .apply {
            this.interpolator = AccelerateDecelerateInterpolator()
            this.duration = duration
            addUpdateListener {
                view.invalidate()
            }
            onEnd?.let {
                doOnEnd { it() }
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

fun Matrix.getCopy(): Matrix =
    Matrix().apply {
        set(this@getCopy)
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