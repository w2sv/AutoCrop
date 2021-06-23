package utils

import android.os.SystemClock
import androidx.test.espresso.PerformException
import androidx.test.espresso.intent.Intents
import java.util.concurrent.TimeoutException

inline fun intentTester(wrappedFun: () -> Unit) {
    Intents.init()
    wrappedFun()
    Intents.release()
}

inline fun retryFlakyAction(timeout: Long, flakyAction: () -> Unit) {
    val startTime = SystemClock.elapsedRealtime()
    var nTries = 0

    while (SystemClock.elapsedRealtime() - startTime < timeout) {
        try {
            return flakyAction()
        } catch (e: PerformException) {
            nTries += 1
        }
    }

    throw TimeoutException("Timed out after $nTries tries in $timeout ms")
}