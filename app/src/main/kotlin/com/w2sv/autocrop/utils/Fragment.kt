package com.w2sv.autocrop.utils

import android.app.Activity
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

fun <F : Fragment> getFragment(clazz: Class<F>, vararg bundlePairs: Pair<String, Any?>): F =
    clazz.getDeclaredConstructor().newInstance()
        .apply {
            arguments = bundleOf(*bundlePairs)
        }

@Suppress("UNCHECKED_CAST")
fun <A : Activity> Fragment.requireCastActivity(): A =
    requireActivity() as A

val Fragment.tagName: String
    get() = this::class.java.simpleName

fun Fragment.registerOnBackPressedCallback(onBackPressedCallback: OnBackPressedCallback) {
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
}

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