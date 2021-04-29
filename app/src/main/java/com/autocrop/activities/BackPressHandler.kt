package com.autocrop.activities

import android.os.Handler


class BackPressHandler {
    private val resetDuration: Long = 2500

    var pressedOnce: Boolean = false

    fun onPress() {
        pressedOnce = true

        Handler().postDelayed(
            { pressedOnce = false },
            resetDuration
        )
    }
}