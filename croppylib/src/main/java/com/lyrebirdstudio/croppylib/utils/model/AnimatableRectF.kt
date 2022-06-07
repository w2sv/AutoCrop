package com.lyrebirdstudio.croppylib.utils.model

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.Keep

/**
 * Keeping of setters required for [animateTo]
 */
class AnimatableRectF : RectF() {

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

    fun animateTo(target: AnimatableRectF, onUpdate: (RectF) -> Unit = {}) {
        val animateLeft = ObjectAnimator.ofFloat(this, "left", left, target.left)
        val animateRight = ObjectAnimator.ofFloat(this, "right", right, target.right)
        val animateTop = ObjectAnimator.ofFloat(this, "top", top, target.top)
        val animateBottom = ObjectAnimator.ofFloat(this, "bottom", bottom, target.bottom)
        animateBottom.addUpdateListener {
            onUpdate.invoke(this)
        }

        AnimatorSet()
            .apply {
                playTogether(animateLeft, animateRight, animateTop, animateBottom)
                interpolator = AccelerateDecelerateInterpolator()
                duration = 300
            }
            .start()
    }
}