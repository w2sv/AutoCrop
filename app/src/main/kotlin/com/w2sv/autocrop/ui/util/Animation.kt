package com.w2sv.autocrop.ui.util

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.Animation
import com.w2sv.androidutils.view.hide
import com.w2sv.androidutils.view.show

abstract class SimpleAnimationListener : Animation.AnimationListener {
    override fun onAnimationStart(animation: Animation?) {}
    override fun onAnimationEnd(animation: Animation?) {}
    override fun onAnimationRepeat(animation: Animation?) {}
}

fun Animation.doOnEnd(block: Animation.() -> Unit): Animation =
    apply {
        setAnimationListener(
            object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    block()
                }
            }
        )
    }

fun Animation.doOnStart(block: Animation.() -> Unit): Animation =
    apply {
        setAnimationListener(
            object : SimpleAnimationListener() {
                override fun onAnimationStart(animation: Animation?) {
                    block()
                }
            }
        )
    }

abstract class SimpleAnimatorListener : AnimatorListener {
    override fun onAnimationStart(animation: Animator) {}
    override fun onAnimationEnd(animation: Animator) {}
    override fun onAnimationCancel(animation: Animator) {}
    override fun onAnimationRepeat(animation: Animator) {}
}

fun ViewPropertyAnimator.doOnStart(block: Animator.() -> Unit) =
    apply {
        setListener(
            object : SimpleAnimatorListener() {
                override fun onAnimationStart(animation: Animator) {
                    block(animation)
                }
            }
        )
    }

fun ViewPropertyAnimator.doOnEnd(block: Animator.() -> Unit): ViewPropertyAnimator =
    apply {
        setListener(
            object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator) {
                    block(animation)
                }
            }
        )
    }

object AnimationDuration {
    const val SHORT = 250L
    const val MEDIUM = 500L
    const val LONG = 1000L
}

fun View.fadeIn(duration: Long = AnimationDuration.MEDIUM): ViewPropertyAnimator =
    animate().alpha(1f).setDuration(duration).doOnStart { show() }

fun View.fadeOut(duration: Long = AnimationDuration.MEDIUM): ViewPropertyAnimator =
    animate().alpha(0f).setDuration(duration).doOnEnd { hide() }