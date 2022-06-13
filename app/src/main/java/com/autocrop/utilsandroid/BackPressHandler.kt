package com.autocrop.utilsandroid

import android.os.Handler
import android.os.Looper
import de.mateware.snacky.Snacky

/**
 * Display Snackbar beset with $snackbarMessage on first backpress,
 * upon second within $RESET_DURATION trigger $secondPressAction,
 */
class BackPressHandler(
    private val onFirstPressNotificationSnackyBuilder: Snacky.Builder,
    private val onSecondPress: () -> Unit) {

    var pressedOnce: Boolean = false

    operator fun invoke() {
        if (pressedOnce)
            return onSecondPress()

        pressedOnce = true
        onFirstPressNotificationSnackyBuilder
            .buildAndShow()

        Handler(Looper.getMainLooper()).postDelayed(
            { pressedOnce = false },
            2500
        )
    }
}