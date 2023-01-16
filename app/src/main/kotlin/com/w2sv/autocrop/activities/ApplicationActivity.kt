package com.w2sv.autocrop.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class ApplicationActivity : FragmentHostingActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerObserversAndCallbacks()

        if (savedInstanceState == null)
            launchRootFragment()
    }

    protected open val lifecycleObservers: List<LifecycleObserver>
        get() = listOf()

    private fun registerObserversAndCallbacks() {
        lifecycleObservers.forEach {
            lifecycle.addObserver(it)
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    protected abstract val onBackPressedCallback: OnBackPressedCallback
}