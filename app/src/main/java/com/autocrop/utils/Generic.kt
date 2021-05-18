package com.autocrop.utils

import java.lang.ref.WeakReference


fun <T> WeakReference<T>.forceUnwrapped(): T = get()!!

fun Boolean.toInt(): Int = compareTo(false)

fun Any?.notNull(): Boolean = this != null