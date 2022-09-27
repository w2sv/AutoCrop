package com.autocrop.utils.kotlin

import timber.log.Timber

inline fun<T> logBeforehand(message: String, f: () -> T): T {
    Timber.i(message)
    return f()
}