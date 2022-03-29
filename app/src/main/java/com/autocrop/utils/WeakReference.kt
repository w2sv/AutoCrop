package com.autocrop.utils

import java.lang.ref.WeakReference

fun <T> WeakReference<T>.forceUnwrapped(): T = get()!!