package com.w2sv.autocrop.ui.util

import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MainThread
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment

@MainThread
fun Fragment.registerOnBackPressedHandler(handleOnBackPressed: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleOnBackPressed()
            }
        }
    )
}

fun Fragment.postponeEnterTransition(view: View) {
    postponeEnterTransition()

    view.doOnPreDraw {
        startPostponedEnterTransition()
    }
}
