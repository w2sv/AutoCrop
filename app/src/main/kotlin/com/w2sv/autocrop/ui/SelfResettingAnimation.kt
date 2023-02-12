package com.w2sv.autocrop.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.daimajia.androidanimations.library.YoYo

class SelfResettingAnimation(
    private val animationComposer: YoYo.AnimationComposer,
    lifecycleOwner: LifecycleOwner
) : DefaultLifecycleObserver {

    private var animation: YoYo.YoYoString? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        animation?.stop(true)
    }

    fun play() {
        animation = animationComposer.play()
    }
}