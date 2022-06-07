package com.autocrop.utilsandroid

import android.app.Activity
import android.os.Handler
import android.os.Looper

/**
 * Display Snackbar beset with $snackbarMessage on first backpress,
 * upon second within $RESET_DURATION trigger $secondPressAction,
 */
class BackPressHandler(
    private val activity: Activity,
    private val snackbarMessage: String,
    private val onSecondPress: () -> Unit) {

    companion object {
        private const val RESET_DURATION: Long = 2500
    }

    var pressedOnce: Boolean = false

    operator fun invoke() {
        if (pressedOnce)
            return onSecondPress()

        pressedOnce = true

        activity
            .snacky(snackbarMessage)
            .buildAndShow()

        Handler(Looper.getMainLooper()).postDelayed(
            { pressedOnce = false },
            RESET_DURATION
        )
    }
}