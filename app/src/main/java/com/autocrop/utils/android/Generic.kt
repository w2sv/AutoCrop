package com.autocrop.utils.android

import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.w2sv.autocrop.BuildConfig


fun intentExtraIdentifier(name: String): String = "com.autocrop.$name"

fun debuggingModeEnabled(): Boolean = BuildConfig.DEBUG

fun hideSystemUI(window: Window) =
    if (apiNotNewerThanQ)
        ViewCompat.getWindowInsetsController(window.decorView)?.run {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    else
        window.setDecorFitsSystemWindows(false)
