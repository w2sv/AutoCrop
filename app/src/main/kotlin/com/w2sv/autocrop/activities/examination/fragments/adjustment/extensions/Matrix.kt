package com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.view.animation.AccelerateDecelerateInterpolator

fun Matrix.setToTargetAnimatedly(
    dst: Matrix,
    onUpdate: () -> Unit = {}
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
        }
        .apply { interpolator = AccelerateDecelerateInterpolator() }
        .apply { duration = 300 }
        .start()
}

fun Matrix.getScaleX(): Float =
    values()[Matrix.MSCALE_X]

fun Matrix.getScaleY(): Float =
    values()[Matrix.MSCALE_Y]

fun Matrix.getTranslateX(): Float =
    values()[Matrix.MTRANS_X]

fun Matrix.getTranslateY(): Float =
    values()[Matrix.MTRANS_Y]

fun Matrix.clone(): Matrix =
    Matrix().apply {
        set(this@clone)
    }

fun Matrix.getInverse(): Matrix {
    val inverse = Matrix()
    invert(inverse)
    return inverse
}

private fun Matrix.values(): FloatArray =
    FloatArray(9).apply {
        getValues(this)
    }