package com.w2sv.autocrop.ui.util.view

import android.animation.TimeInterpolator
import android.content.Context
import android.view.animation.OvershootInterpolator
import androidx.annotation.TransitionRes
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter

fun Transition.onEnd(callback: () -> Unit): Transition =
    addListener(
        object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                callback()
            }
        }
    )

fun Transition.onStart(callback: () -> Unit): Transition =
    addListener(
        object : TransitionListenerAdapter() {
            override fun onTransitionStart(transition: Transition) {
                callback()
            }
        }
    )

enum class SharedElementTransitionState {
    Entering,
    Idle,
    Exiting;

    val isIdle get() = this == Idle
}

fun inflateSharedElementTransition(
    context: Context,
    @TransitionRes resource: Int = android.R.transition.move,
    duration: Long = 700,
    interpolator: TimeInterpolator = OvershootInterpolator()
): Transition? =
    inflateTransition(context, resource)?.apply {
        this.duration = duration
        this.interpolator = interpolator
    }

fun inflateTransition(context: Context, @TransitionRes resource: Int): Transition? =
    TransitionInflater
        .from(context)
        .inflateTransition(resource)
