package com.w2sv.autocrop.ui.views

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.daimajia.androidanimations.library.YoYo

interface AnimationHandler: DefaultLifecycleObserver {

    var animation: YoYo.YoYoString?

    class Implementation : DefaultLifecycleObserver,
                           AnimationHandler {

        override var animation: YoYo.YoYoString? = null

        override fun onResume(owner: LifecycleOwner) {
            super<AnimationHandler>.onResume(owner)

            animation?.stop(true)
        }
    }
}