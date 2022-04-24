package com.autocrop.uielements.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R

fun crossFade(animationDuration: Long, fadeOutView: View, vararg fadeInView: View){
    fadeInView.forEach {
        it.fadeIn(animationDuration)
    }
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

fun View.animate(technique: Techniques, onEnd: (() -> Unit)? = null): YoYo.YoYoString =
    YoYo.with(technique)
        .duration(context.resources.getInteger(R.integer.yoyo_animation_duration).toLong())
        .apply {
            onEnd?.let {
                onEnd {it()}
            }
        }
        .playOn(this)