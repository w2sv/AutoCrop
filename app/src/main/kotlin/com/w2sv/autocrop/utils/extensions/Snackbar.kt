package com.w2sv.autocrop.utils.extensions

import com.google.android.material.snackbar.Snackbar
import com.w2sv.androidutils.extensions.launchDelayed
import kotlinx.coroutines.CoroutineScope

fun Snackbar.onHalfwayShown(coroutineScope: CoroutineScope, block: suspend () -> Unit): Snackbar =
    apply {
        coroutineScope.launchDelayed((duration / 2).toLong()) {
            block()
        }
    }