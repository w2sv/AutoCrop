package com.w2sv.autocrop.utils.android.extensions

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.w2sv.autocrop.utils.kotlin.BlankFun
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo

private const val DEFAULT_VIEW_ANIMATION_DURATION = 1000L

fun View.animate(
    technique: Techniques,
    duration: Long = DEFAULT_VIEW_ANIMATION_DURATION,
    delay: Long = 0L,
    onEnd: BlankFun? = null
): YoYo.YoYoString =
    YoYo.with(technique)
        .delay(delay)
        .duration(duration)
        .apply {
            onEnd?.let {
                onEnd { it() }
            }
        }
        .playOn(this)

//$$$$$$$$$$$$$$$$$$$$$$
// Visibility Changing $
//$$$$$$$$$$$$$$$$$$$$$$

fun crossFade(fadeOutViews: Array<View>, fadeInViews: Array<View>, duration: Long = DEFAULT_VIEW_ANIMATION_DURATION) {
    fadeInViews.forEach {
        it.fadeIn(duration)
    }
    fadeOutViews.forEach {
        it.fadeOut(duration)
    }
}

fun View.fadeIn(duration: Long = DEFAULT_VIEW_ANIMATION_DURATION): YoYo.YoYoString =
    apply {
        show()
    }
        .animate(Techniques.FadeIn, duration)

fun View.fadeOut(duration: Long = DEFAULT_VIEW_ANIMATION_DURATION, delay: Long = 0): YoYo.YoYoString =
    animate(Techniques.FadeOut, duration, delay = delay) {
        hide()
    }

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

inline fun View.ifNotInEditMode(f: BlankFun) {
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