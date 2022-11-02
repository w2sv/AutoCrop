package com.w2sv.autocrop.utils.android

import android.os.Handler
import android.os.Looper
import com.w2sv.kotlinutils.UnitFun
import de.mateware.snacky.Snacky

/**
 * Shows [onFirstPressNotificationSnackyBuilder] on first back press,
 * on second within [confirmationWindowDuration] trigger [onSecondPress],
 */
class BackPressHandler(
    private val onFirstPressNotificationSnackyBuilder: Snacky.Builder,
    private val confirmationWindowDuration: Long = 2500,
    private val onSecondPress: UnitFun
) {
    private var pressedOnce: Boolean = false

    operator fun invoke() {
        if (pressedOnce)
            return onSecondPress()

        pressedOnce = true
        onFirstPressNotificationSnackyBuilder
            .build()
            .show()

        Handler(Looper.getMainLooper())
            .postDelayed(
                {
                    pressedOnce = false
                },
                confirmationWindowDuration
            )
    }
}