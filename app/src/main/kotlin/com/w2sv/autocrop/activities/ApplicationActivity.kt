package com.w2sv.autocrop.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleObserver

abstract class ApplicationActivity : FragmentedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerObserversAndCallbacks()

        if (savedInstanceState == null)
            launchRootFragment()
    }

    private fun registerObserversAndCallbacks() {
        lifecycleObservers?.forEach {
            lifecycle.addObserver(it)
        }
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    this@ApplicationActivity.handleOnBackPressed()
                }
            }
        )
    }

    protected open val lifecycleObservers: List<LifecycleObserver>?
        get() = null

    protected abstract fun handleOnBackPressed()
}