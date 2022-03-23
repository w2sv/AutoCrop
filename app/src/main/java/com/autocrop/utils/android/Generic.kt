package com.autocrop.utils.android

import android.view.View
import android.view.Window
import com.w2sv.autocrop.BuildConfig


fun intentExtraIdentifier(name: String): String = "com.autocrop.$name"

fun debuggingModeEnabled(): Boolean = BuildConfig.DEBUG

fun hideSystemUI(window: Window) {
    if (apiNotNewerThanQ)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    else
        window.setDecorFitsSystemWindows(false)
}
