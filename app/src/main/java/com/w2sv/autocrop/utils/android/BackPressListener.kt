package com.w2sv.autocrop.utils.android

import com.w2sv.kotlinutils.UnitFun
import com.w2sv.kotlinutils.extensions.launchDelayed
import kotlinx.coroutines.CoroutineScope

class BackPressListener(
    private val coroutineScope: CoroutineScope,
    private val confirmationWindowDuration: Long = 2500,
) {
    private var pressedOnce: Boolean = false

    operator fun invoke(onFirstPress: UnitFun, onSecondPress: UnitFun) {
        if (pressedOnce) {
            pressedOnce = false
            onSecondPress()
        }
        else {
            pressedOnce = true
            onFirstPress()

            coroutineScope.launchDelayed(confirmationWindowDuration) {
                pressedOnce = false
            }
        }
    }
}