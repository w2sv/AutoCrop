package com.w2sv.common

import com.w2sv.androidutils.coroutines.launchDelayed
import kotlinx.coroutines.CoroutineScope

class BackPressHandler(
    private val coroutineScope: CoroutineScope,
    private val confirmationWindowDuration: Long,
) {
    private var pressedOnce: Boolean = false

    operator fun invoke(onFirstPress: () -> Unit, onSecondPress: () -> Unit) {
        when (pressedOnce) {
            true -> {
                pressedOnce = false
                onSecondPress()
            }

            false -> {
                pressedOnce = true
                onFirstPress()

                coroutineScope.launchDelayed(confirmationWindowDuration) {
                    pressedOnce = false
                }
            }
        }
    }
}