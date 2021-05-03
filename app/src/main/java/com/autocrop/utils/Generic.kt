package com.autocrop.utils

import java.lang.ref.WeakReference


fun Boolean.toInt(): Int = compareTo(false)

fun <T> WeakReference<T>.forceUnwrapped(): T {
    return get()!!
}