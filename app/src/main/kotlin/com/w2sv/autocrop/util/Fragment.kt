package com.w2sv.autocrop.util

import androidx.activity.OnBackPressedCallback
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

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

fun Fragment.navController(): Lazy<NavController> {
    return lazy {
        findNavController()
    }
}