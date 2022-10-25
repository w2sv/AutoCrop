package com.autocrop.activities.iodetermination.fragments.manualcrop.utils.model

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.Keep
import androidx.core.animation.doOnEnd

class AnimatableRectF : RectF() {
    fun animateTo(target: AnimatableRectF, onUpdate: (RectF) -> Unit = {}) {
        val animateLeft = ObjectAnimator.ofFloat(this, "left", left, target.left)
        val animateRight = ObjectAnimator.ofFloat(this, "right", right, target.right)
        val animateTop = ObjectAnimator.ofFloat(this, "top", top, target.top)
        val animateBottom = ObjectAnimator.ofFloat(this, "bottom", bottom, target.bottom)

        AnimatorSet()
            .apply {
                playTogether(animateLeft, animateRight, animateTop, animateBottom)
                interpolator = AccelerateDecelerateInterpolator()
                duration = 300
                doOnEnd { onUpdate(this@AnimatableRectF) }
            }
            .start()
    }

    /**
     * Keeping of setters required for [animateTo]
     */

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