package com.w2sv.autocrop.activities.examination.fragments.manualcrop.extensions

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.view.animation.AccelerateDecelerateInterpolator

fun Matrix.animateToMatrix(
    targetMatrix: Matrix,
    onUpdate: () -> Unit = {}
) {
    val scaleAnimator = ValueAnimator.ofFloat(this.getScaleX(), targetMatrix.getScaleX())
    val translateXAnimator =
        ValueAnimator.ofFloat(this.getTranslateX(), targetMatrix.getTranslateX())
    val translateYAnimator =
        ValueAnimator.ofFloat(this.getTranslateY(), targetMatrix.getTranslateY())

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
        onUpdate.invoke()
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

private fun Matrix.values(): FloatArray =
    FloatArray(9).apply {
        getValues(this)
    }