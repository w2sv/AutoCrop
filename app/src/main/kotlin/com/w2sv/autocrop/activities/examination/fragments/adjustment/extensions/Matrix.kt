package com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.view.animation.AccelerateDecelerateInterpolator

fun Matrix.animateToTarget(
    dst: Matrix,
    duration: Long,
    onUpdate: () -> Unit
) {
    val scaleAnimator = ValueAnimator.ofFloat(this.getScaleX(), dst.getScaleX())
    val translateXAnimator =
        ValueAnimator.ofFloat(this.getTranslateX(), dst.getTranslateX())
    val translateYAnimator =
        ValueAnimator.ofFloat(this.getTranslateY(), dst.getTranslateY())

    translateYAnimator.addUpdateListener {
        reset()
        preScale(
            scaleAnimator.animatedValue as Float,
            scaleAnimator.animatedValue as Float
        )
        postTranslate(
            translateXAnimator.animatedValue as Float,
            translateYAnimator.animatedValue as Float
        )
        onUpdate()
    }

    AnimatorSet()
        .apply {
            playTogether(
                scaleAnimator,
                translateXAnimator,
                translateYAnimator
            )
            interpolator = AccelerateDecelerateInterpolator()
            this.duration = duration
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