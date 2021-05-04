package com.autocrop.utils

import java.lang.ref.WeakReference


fun Boolean.toInt(): Int = compareTo(false)

fun <T> WeakReference<T>.forceUnwrapped(): T {
    return get()!!
}

fun Int.smallerThan(other: Int): Boolean = compareTo(other) == -1
fun Int.biggerThan(other: Int): Boolean = compareTo(other) == 1
fun Int.smallerEquals(other: Int): Boolean = compareTo(other).run { equals(0) || equals(-1) }
fun Int.biggerEquals(other: Int): Boolean = compareTo(other).run { equals(0) || equals(1) }