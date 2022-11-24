package utils.espresso

import androidx.test.espresso.intent.Intents

inline fun intentTester(wrappedFun: () -> Unit) {
    Intents.init()
    wrappedFun()
    Intents.release()
}