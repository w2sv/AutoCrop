package com.w2sv.autocrop.util

import android.view.animation.Animation

abstract class SimpleAnimationListener : Animation.AnimationListener {
    override fun onAnimationStart(animation: Animation?) {}
    override fun onAnimationEnd(animation: Animation?) {}
    override fun onAnimationRepeat(animation: Animation?) {}
}

fun Animation.doOnEnd(block: Animation.() -> Unit) {
    setAnimationListener(
        object : SimpleAnimationListener() {
            override fun onAnimationEnd(animation: Animation?) {
                block()
            }
        }
    )
}