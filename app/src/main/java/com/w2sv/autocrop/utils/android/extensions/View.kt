package com.w2sv.autocrop.utils.android.extensions

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R
import com.w2sv.kotlinutils.UnitFun
import dagger.hilt.android.internal.managers.ViewComponentManager

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

inline fun View.ifNotInEditMode(f: UnitFun) {
    if (!isInEditMode)
        f()
}

inline fun <reified VM : ViewModel> View.viewModel(): Lazy<VM> =
    lazy { ViewModelProvider(findViewTreeViewModelStoreOwner()!!)[VM::class.java] }

inline fun <reified VM : ViewModel> View.viewModelImmediate(): VM =
    viewModel<VM>().value

inline fun <reified VM : ViewModel> View.activityViewModel(): Lazy<VM> =
    lazy {
        ViewModelProvider(
            (context.activityContext() as ViewModelStoreOwner)
        )[VM::class.java]
    }

fun Context.activityContext(): Context =
    (this as? ViewComponentManager.FragmentContextWrapper)?.baseContext
        ?: this

inline fun <reified VM : ViewModel> View.activityViewModelImmediate(): VM =
    activityViewModel<VM>().value