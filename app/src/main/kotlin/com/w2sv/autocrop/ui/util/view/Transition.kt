package com.w2sv.autocrop.ui.util.view

import android.content.Context
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

fun inflateTransition(context: Context, @TransitionRes resource: Int): Transition? =
    TransitionInflater
        .from(context)
        .inflateTransition(resource)
