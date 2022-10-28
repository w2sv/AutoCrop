package com.autocrop.utils.android

import android.os.Handler
import android.os.Looper
import com.autocrop.utils.android.extensions.buildAndShow
import de.mateware.snacky.Snacky

/**
 * Display Snackbar beset with $snackbarMessage on first back press,
 * upon second within $RESET_DURATION trigger $secondPressAction,
 */
class BackPressHandler(
    private val onFirstPressNotificationSnackyBuilder: Snacky.Builder,
    private val onSecondPress: () -> Unit,
    private val confirmationWindowDuration: Long = 2500) {

    var pressedOnce: Boolean = false

    operator fun invoke() {
        if (pressedOnce)
            return onSecondPress()

        pressedOnce = true
        onFirstPressNotificationSnackyBuilder
            .buildAndShow()

        Handler(Looper.getMainLooper()).postDelayed(
            { pressedOnce = false },
            confirmationWindowDuration
        )
    }
}