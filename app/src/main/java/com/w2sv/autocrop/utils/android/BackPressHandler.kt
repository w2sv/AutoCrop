package com.w2sv.autocrop.utils.android

import com.w2sv.autocrop.utils.UnitFun
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
            .build().show()

        postDelayed(confirmationWindowDuration) {
            pressedOnce = false
        }
    }
}