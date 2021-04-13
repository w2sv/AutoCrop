package com.autocrop.activities

import android.app.Activity
import android.view.View
import android.view.Window


// TODO
abstract class SystemUIHider: Activity() {
    override fun onStart() {
        super.onStart()
        hideSystemUI(window)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI(window)
    }
}


fun hideSystemUI(window: Window) {
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
}