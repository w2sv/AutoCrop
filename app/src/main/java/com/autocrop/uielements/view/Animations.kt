package com.autocrop.uielements.view

import android.view.View
import com.autocrop.utils.BlankFun
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R

fun View.animate(technique: Techniques, onEnd: BlankFun? = null): YoYo.YoYoString =
    YoYo.with(technique)
        .duration(context.resources.getInteger(R.integer.view_animation_duration).toLong())
        .apply {
            onEnd?.let {
                onEnd {it()}
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

fun View.fadeIn(){
    show()
    animate(Techniques.FadeIn)
}

fun View.fadeOut(){
    animate(Techniques.FadeOut)
}
