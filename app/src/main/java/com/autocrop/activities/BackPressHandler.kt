package com.autocrop.activities

import android.os.Handler


class BackPressHandler {
    companion object {
        private const val RESET_DURATION: Long = 2500
    }

    var pressedOnce: Boolean = false

    fun onPress() {
        pressedOnce = true

        Handler().postDelayed(
            { pressedOnce = false },
            RESET_DURATION
        )
    }
}