package com.w2sv.autocrop.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleObserver

abstract class AppActivity : ViewBoundFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerObservers(lifecycleObservers, ::handleOnBackPressed)

        if (savedInstanceState == null)
            launchRootFragment()
    }

    protected open val lifecycleObservers: List<LifecycleObserver>?
        get() = null

    protected abstract fun handleOnBackPressed()
}

fun ComponentActivity.registerObservers(
    lifecycleObservers: Iterable<LifecycleObserver>?,
    handleOnBackPressed: () -> Unit
) {
    lifecycleObservers?.forEach {
        lifecycle.addObserver(it)
    }
    onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleOnBackPressed()
            }
        }
    )
}