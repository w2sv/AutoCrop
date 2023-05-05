package com.w2sv.autocrop.utils.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.coroutines.launchDelayed
import kotlinx.coroutines.CoroutineScope

fun LifecycleOwner.launchAfterShortDelay(block: CoroutineScope.() -> Unit) {
    lifecycleScope.launchDelayed(200, block = block)
}