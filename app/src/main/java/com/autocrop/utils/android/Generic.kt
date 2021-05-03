package com.autocrop.utils.android

import android.os.Build
import com.bunsenbrenner.screenshotboundremoval.BuildConfig
import java.lang.ref.WeakReference


fun intentExtraIdentifier(name: String): String = "com.autocrop.$name"


fun debuggingModeEnabled(): Boolean = BuildConfig.DEBUG

fun apiLowerEquals(apiNumber: Int): Boolean = Build.VERSION.SDK_INT <= apiNumber

fun <T> WeakReference<out T>.forceUnwrapped(): T {
    return get()!!
}