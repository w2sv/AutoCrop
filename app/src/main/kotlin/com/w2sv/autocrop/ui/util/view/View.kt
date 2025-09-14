package com.w2sv.autocrop.ui.util.view

import android.view.View

// TODO: move to AndroidUtils
fun View.setDebouncedOnClickListener(interval: Long = 500L, onClick: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { v ->
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= interval) {
            lastClickTime = now
            onClick(v)
        }
    }
}
