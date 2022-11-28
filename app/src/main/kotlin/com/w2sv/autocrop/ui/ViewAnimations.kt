package com.w2sv.autocrop.ui

import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.hide
import com.w2sv.androidutils.extensions.remove
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R

fun crossVisualize(hideView: View, showView: View){
    hideView.hide()
    showView.show()
}

fun View.animate(
    technique: Techniques,
    duration: Long? = null,
    delay: Long = 0L
): YoYo.YoYoString =
    animationComposer(technique, duration, delay)
        .playOn(this)

fun View.animationComposer(
    technique: Techniques,
    duration: Long? = null,
    delay: Long = 0L
): YoYo.AnimationComposer =
    YoYo.with(technique)
        .delay(delay)
        .duration(
            duration
                ?: resources.getLong(R.integer.duration_view_animation)
        )

fun crossFade(fadeOut: View, fadeIn: View, duration: Long? = null) {
    fadeOut.fadeOut(duration)
    fadeIn.fadeIn(duration)
}

fun View.fadeIn(duration: Long? = null): YoYo.YoYoString =
    apply {
        show()
    }
        .animate(Techniques.FadeIn, duration)

fun View.fadeOut(duration: Long? = null, delay: Long = 0): YoYo.YoYoString =
    animationComposer(Techniques.FadeOut, duration, delay)
        .onEnd {
            remove()
        }
        .playOn(this)