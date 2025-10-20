package com.w2sv.autocrop.ui.util.view

import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.TransitionRes
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter

fun Transition.onTransitionEnd(callback: () -> Unit): Transition =
    addListener(
        object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                callback()
            }
        }
    )

fun inflateTransition(context: Context, @TransitionRes resource: Int): Transition? =
    TransitionInflater
        .from(context)
        .inflateTransition(resource)
