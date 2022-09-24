package com.autocrop.utils.kotlin

import timber.log.Timber

inline fun<T> logAfterwards(message: String, f: () -> T): T = f().also { Timber.i(message) }
inline fun<T> logBeforehand(message: String, f: () -> T): T {
    Timber.i(message)
    return f()
}