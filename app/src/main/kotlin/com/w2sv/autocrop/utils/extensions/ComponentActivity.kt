package com.w2sv.autocrop.utils.extensions

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleObserver

fun ComponentActivity.registerOnBackPressedListener(
    onBackPressed: () -> Unit
) {
    onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    )
}

fun ComponentActivity.addLifecycleObservers(observers: Iterable<LifecycleObserver>){
    observers.forEach(lifecycle::addObserver)
}