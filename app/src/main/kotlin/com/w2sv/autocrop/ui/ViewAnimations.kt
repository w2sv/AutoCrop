package com.w2sv.autocrop.ui

import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R

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

fun fadeIn(vararg view: View, duration: Long? = null){
    view.forEach {
        it.fadeIn(duration)
    }
}

fun fadeOut(vararg view: View, duration: Long? = null){
    view.forEach {
        it.fadeOut(duration)
    }
}

fun View.fadeIn(duration: Long? = null): YoYo.YoYoString =
    apply {
        show()
    }
        .animate(Techniques.FadeIn, duration)

fun View.fadeOut(duration: Long? = null, delay: Long = 0, onEndVisibility: Int = View.GONE): YoYo.YoYoString =
    animationComposer(Techniques.FadeOut, duration, delay)
        .onEnd {
            visibility = onEndVisibility
        }
        .playOn(this)