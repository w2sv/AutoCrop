package utils.espresso

import android.os.SystemClock
import androidx.test.espresso.PerformException
import java.util.concurrent.TimeoutException

const val MEDIUM_TIMEOUT: Long = 1000
const val SLOW_TIMEOUT: Long = 5000

inline fun retryFlakyAction(timeout: Long = MEDIUM_TIMEOUT, flakyAction: () -> Unit) {
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
