package com.autocrop.activities

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.autocrop.utils.android.TextColors
import com.autocrop.utils.android.displaySnackbar


/**
 * Display Snackbar beset with $snackbarMessage on first backpress,
 * upon second within $RESET_DURATION trigger $secondPressAction,
 */
class BackPressHandler(
    private val snackbarDisplayActivity: Activity,
    private val snackbarMessage: String,
    private val onSecondPress: () -> Unit) {

    companion object {
        private const val RESET_DURATION: Long = 2500
    }

    var pressedOnce: Boolean = false

    operator fun invoke() {
        if (!pressedOnce){
            pressedOnce = true

            Handler(Looper.getMainLooper()).postDelayed(
                { pressedOnce = false },
                RESET_DURATION
            )

            snackbarDisplayActivity.displaySnackbar(snackbarMessage, TextColors.NEUTRAL)
        }
        else
            return onSecondPress()
    }
}