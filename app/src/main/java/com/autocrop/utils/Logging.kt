package com.autocrop.utils

import timber.log.Timber

inline fun<T> logAfterwards(message: String, f: () -> T): T = f().also { Timber.i(message) }