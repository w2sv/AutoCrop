package com.w2sv.autocrop.utils.android

import com.w2sv.autocrop.utils.android.extensions.buildAndShow
import com.w2sv.autocrop.utils.kotlin.VoidFun
import de.mateware.snacky.Snacky

/**
 * Shows [onFirstPressNotificationSnackyBuilder] on first back press,
 * on second within [confirmationWindowDuration] trigger [onSecondPress],
 */
class BackPressHandler(
    private val onFirstPressNotificationSnackyBuilder: Snacky.Builder,
    private val confirmationWindowDuration: Long = 2500,
    private val onSecondPress: VoidFun
) {
    private var pressedOnce: Boolean = false

    operator fun invoke() {
        if (pressedOnce)
            return onSecondPress()

        pressedOnce = true
        onFirstPressNotificationSnackyBuilder
            .buildAndShow()

        postDelayed(confirmationWindowDuration) {
            pressedOnce = false
        }
    }
}