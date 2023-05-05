package com.w2sv.autocrop.ui.views

import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.androidutils.ui.views.hide
import com.w2sv.androidutils.ui.views.show
import kotlinx.coroutines.CoroutineScope

fun View.animate(
    technique: Techniques,
    duration: Long? = null
): YoYo.YoYoString =
    getAnimationComposer(technique, duration)
        .play()

fun View.getAnimationComposer(
    technique: Techniques,
    duration: Long? = null
): YoYo.AnimationComposer =
    YoYo.with(technique, this)
        .apply {
            duration?.let {
                duration(it)
            }
        }

fun YoYo.AnimationComposer.onHalfwayFinished(
    coroutineScope: CoroutineScope,
    block: suspend CoroutineScope.() -> Unit
): YoYo.AnimationComposer =
    apply {
        coroutineScope.launchDelayed(duration / 2) {
            block()
        }
    }

fun crossFade(fadeOut: View, fadeIn: View, duration: Long? = null) {
    fadeOut.fadeOut(duration)
    fadeIn.fadeIn(duration)
}

fun fadeIn(vararg view: View, duration: Long? = null) {
    view.forEach {
        it.fadeIn(duration)
    }
}

fun fadeOut(vararg view: View, duration: Long? = null) {
    view.forEach {
        it.fadeOut(duration)
    }
}

fun View.fadeIn(duration: Long? = null): YoYo.YoYoString =
    fadeInAnimationComposer(duration)
        .play()

fun View.fadeInAnimationComposer(duration: Long? = null): YoYo.AnimationComposer =
    run {
        show()
        getAnimationComposer(Techniques.FadeIn, duration)
    }

fun View.fadeOut(duration: Long? = null, delay: Long = 0): YoYo.YoYoString =
    fadeOutAnimationComposer(duration, delay)
        .play()

fun View.fadeOutAnimationComposer(duration: Long? = null, delay: Long = 0): YoYo.AnimationComposer =
    getAnimationComposer(Techniques.FadeOut, duration)
        .delay(delay)
        .onEnd {
            hide()
        }
