package com.w2sv.autocrop.ui.screen.cropadjustment.view.config

import android.animation.ValueAnimator

private const val ALPHA_MAX = 255

class GridAlphaHandler(initialValue: Int, private val onValueUpdate: (Int) -> Unit) {
    var value = initialValue
        private set(value) {
            field = value
            onValueUpdate(value)
        }

    private val gridFadeOutAnimator = ValueAnimator.ofInt(ALPHA_MAX, 0).apply {
        startDelay = 750
        duration = 300
        addUpdateListener { animator ->
            value = animator.animatedValue as Int
        }
    }

    fun resetValue() {
        gridFadeOutAnimator.cancel()
        value = ALPHA_MAX
    }

    fun startFadeOutAnimation() {
        gridFadeOutAnimator.start()
    }
}
