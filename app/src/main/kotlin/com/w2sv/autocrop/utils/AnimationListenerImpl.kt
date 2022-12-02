package com.w2sv.autocrop.utils

import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener

abstract class AnimationListenerImpl : AnimationListener {
    override fun onAnimationStart(animation: Animation?) {}
    override fun onAnimationEnd(animation: Animation?) {}
    override fun onAnimationRepeat(animation: Animation?) {}
}