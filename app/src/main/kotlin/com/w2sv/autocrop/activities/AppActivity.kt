package com.w2sv.autocrop.activities

import android.os.Bundle
import com.w2sv.autocrop.utils.extensions.registerOnBackPressedListener

abstract class AppActivity : ViewBoundFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressedListener(::handleOnBackPressed)
    }

    protected abstract fun handleOnBackPressed()
}