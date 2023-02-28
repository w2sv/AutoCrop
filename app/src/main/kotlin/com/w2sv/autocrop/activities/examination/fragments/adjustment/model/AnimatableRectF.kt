package com.w2sv.autocrop.activities.examination.fragments.adjustment.model

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.Keep
import androidx.core.animation.doOnEnd

//fun animateRectF(obj: View, propertyName: String, src: RectF, dst: RectF, duration: Long, onUpdate: (RectF) -> Unit) {
//    ObjectAnimator.ofObject(obj, propertyName, RectEvaluator(), src, dst)
//        .apply {
//            interpolator = AccelerateDecelerateInterpolator()
//            this.duration = duration
//            doOnEnd { onUpdate(src) }
//        }
//        .start()
//}

class AnimatableRectF(left: Float, top: Float, right: Float, bottom: Float) : RectF(left, top, right, bottom) {

    constructor() : this(0f, 0f, 0f, 0f)

    fun animateTo(target: RectF, duration: Long, onUpdate: () -> Unit) {
        val animateLeft = ObjectAnimator.ofFloat(this, "left", left, target.left)
        val animateRight = ObjectAnimator.ofFloat(this, "right", right, target.right)
        val animateTop = ObjectAnimator.ofFloat(this, "top", top, target.top)
        val animateBottom = ObjectAnimator.ofFloat(this, "bottom", bottom, target.bottom)

        AnimatorSet()
            .apply {
                playTogether(animateLeft, animateRight, animateTop, animateBottom)
                interpolator = AccelerateDecelerateInterpolator()
                this.duration = duration
                doOnEnd { onUpdate() }
            }
            .start()
    }

    // NOTE: Keeping of setters required for [animateTo]

    @Keep
    fun setTop(top: Float) {
        this.top = top
    }

    @Keep
    fun setBottom(bottom: Float) {
        this.bottom = bottom
    }

    @Keep
    fun setRight(right: Float) {
        this.right = right
    }

    @Keep
    fun setLeft(left: Float) {
        this.left = left
    }
}