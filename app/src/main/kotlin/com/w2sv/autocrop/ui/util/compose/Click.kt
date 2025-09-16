package com.w2sv.autocrop.ui.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun debounceClick(interval: Long = 500L, onClick: () -> Unit): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    return {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= interval) {
            lastClickTime = now
            onClick()
        }
    }
}
