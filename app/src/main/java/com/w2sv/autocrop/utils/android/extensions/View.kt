package com.w2sv.autocrop.utils.android.extensions

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R
import com.w2sv.autocrop.utils.VoidFun

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

//$$$$$$$$$$$$$$$$$$$$$$
// Visibility Changing $
//$$$$$$$$$$$$$$$$$$$$$$

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
            hide()
        }
        .playOn(this)

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

inline fun View.ifNotInEditMode(f: VoidFun) {
    if (!isInEditMode)
        f()
}

inline fun <reified VM : ViewModel> View.viewModelLazy(): Lazy<VM> =
    lazy { ViewModelProvider(findViewTreeViewModelStoreOwner()!!)[VM::class.java] }

inline fun <reified VM : ViewModel> View.viewModel(): VM =
    viewModelLazy<VM>().value

inline fun <reified VM : ViewModel> View.activityViewModelLazy(): Lazy<VM> =
    lazy { ViewModelProvider(context as ViewModelStoreOwner)[VM::class.java] }

inline fun <reified VM : ViewModel> View.activityViewModel(): VM =
    activityViewModelLazy<VM>().value