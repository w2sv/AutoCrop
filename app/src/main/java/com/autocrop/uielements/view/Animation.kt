package com.autocrop.uielements.view

import android.view.View
import com.autocrop.utils.BlankFun
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R

private const val DEFAULT_DURATION = -1L

fun View.animate(technique: Techniques,
                 duration: Long = DEFAULT_DURATION,
                 delay: Long? = null,
                 onEnd: BlankFun? = null): YoYo.YoYoString =
    YoYo.with(technique)
        .duration(if (duration != DEFAULT_DURATION) duration else context.resources.getInteger(R.integer.view_animation_duration).toLong())
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

fun crossFade(fadeOutViews: Array<View>, fadeInViews: Array<View>, duration: Long = DEFAULT_DURATION){
    fadeInViews.forEach {
        it.fadeIn(duration)
    }
    fadeOutViews.forEach {
        it.fadeOut(duration)
    }
}

fun View.fadeIn(duration: Long = DEFAULT_DURATION){
    show()
    animate(Techniques.FadeIn, duration)
}

fun View.fadeOut(duration: Long = DEFAULT_DURATION){
    animate(Techniques.FadeOut, duration){
        remove()
    }
}
