package com.autocrop.uielements.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.AnticipateInterpolator
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R

fun View.animate(technique: Techniques, onEnd: (() -> Unit)? = null): YoYo.YoYoString =
    YoYo.with(technique)
        .duration(context.resources.getInteger(R.integer.yoyo_animation_duration).toLong())
        .apply {
            onEnd?.let {
                onEnd {it()}
            }
        }
        .playOn(this)

//$$$$$$$$$$$$$$$$$$$$$$
// Visibility Changing $
//$$$$$$$$$$$$$$$$$$$$$$

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

fun View.shrinkAndFinallyRemove(){
    animate()
        .scaleX(0f)
        .scaleY(0f)
        .setInterpolator(AnticipateInterpolator())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                remove()
            }
        })
        .duration = resources.getInteger(R.integer.visibility_changing_animation_duration).toLong()
}

fun crossFade(animationDuration: Long, fadeOutView: View, vararg fadeInView: View){
    fadeInView.forEach {
        it.fadeIn(animationDuration)
    }
    fadeOutView.fadeOut(animationDuration)
}