package com.autocrop.utils.android

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View


fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.INVISIBLE }
fun View.remove() { visibility = View.GONE}

fun crossFade(fadeInView: View, fadeOutView: View, animationDuration: Long){
    fadeInView.fadeIn(animationDuration)
    fadeOutView.fadeOut(animationDuration)
}

fun View.fadeIn(duration: Long){
    alpha = 0f
    show()

    animate()
        .alpha(1f)
        .duration = duration
}

fun View.fadeOut(duration: Long){
    animate()
        .alpha(0f)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                remove()
            }
        })
        .duration = duration
}