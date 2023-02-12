package com.w2sv.autocrop.activities

import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import com.w2sv.autocrop.utils.extensions.registerOnBackPressedListener

abstract class AppActivity : ViewBoundFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleObservers?.forEach {
            lifecycle.addObserver(it)
        }

        registerOnBackPressedListener(::handleOnBackPressed)

        if (savedInstanceState == null)
            launchRootFragment()
    }

    protected open val lifecycleObservers: List<LifecycleObserver>?
        get() = null

    protected abstract fun handleOnBackPressed()
}