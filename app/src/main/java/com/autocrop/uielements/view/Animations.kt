package com.autocrop.uielements.view

import android.view.View
import com.autocrop.utils.BlankFun
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R

fun View.animate(technique: Techniques, duration: Long? = null, delay: Long? = null, onEnd: BlankFun? = null): YoYo.YoYoString =
    YoYo.with(technique)
        .duration(duration ?: context.resources.getInteger(R.integer.view_animation_duration).toLong())
        .apply {
            onEnd?.let {
                onEnd {it()}
            }
            delay?.let {
                delay(delay)
            }
        }
        .playOn(this)

//$$$$$$$$$$$$$$$$$$$$$$
// Visibility Changing $
//$$$$$$$$$$$$$$$$$$$$$$

fun crossFade(fadeOutView: View, vararg fadeInView: View){
    fadeInView.forEach {
        it.fadeIn()
    }
    fadeOutView.fadeOut()
}

fun View.fadeIn(duration: Long? = null){
    show()
    animate(Techniques.FadeIn, duration)
}

fun View.fadeOut(duration: Long? = null){
    animate(Techniques.FadeOut, duration)
}
