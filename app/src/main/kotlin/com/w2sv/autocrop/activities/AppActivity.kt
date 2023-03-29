package com.w2sv.autocrop.activities

import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import com.w2sv.autocrop.utils.extensions.addLifecycleObservers
import com.w2sv.autocrop.utils.extensions.registerOnBackPressedListener

abstract class AppActivity : ViewBoundFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleObservers?.let {
            addLifecycleObservers(it)
        }

        registerOnBackPressedListener(::handleOnBackPressed)

        if (savedInstanceState == null)
            launchRootFragment()
    }

    protected open val lifecycleObservers: Iterable<LifecycleObserver>?
        get() = null

    protected abstract fun handleOnBackPressed()
}