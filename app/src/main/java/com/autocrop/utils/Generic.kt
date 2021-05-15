package com.autocrop.utils

import java.lang.ref.WeakReference
import kotlin.math.abs


fun <T> WeakReference<T>.forceUnwrapped(): T {
    return get()!!
}

fun Int.smallerThan(other: Int): Boolean = compareTo(other) == -1
fun Int.biggerThan(other: Int): Boolean = compareTo(other) == 1
fun Int.smallerEquals(other: Int): Boolean = compareTo(other).run { equals(0) || equals(-1) }
fun Int.biggerEquals(other: Int): Boolean = compareTo(other).run { equals(0) || equals(1) }

fun <T> Collection<T>.at(index: Int): T =
    if (index < 0)
        elementAt(size + index)
    else
        elementAt(index)


fun <T> Collection<T>.getByBoolean(flag: Boolean): T = elementAt(flag.toInt())
fun Boolean.toInt(): Int = compareTo(false)

fun Any?.notNull(): Boolean = this != null


typealias Index = Int

fun Index.rotated(distance: Int, collectionSize: Int): Int =
    plus(distance).run {
        if (smallerThan(0)) {
            (collectionSize - abs(this) % collectionSize) % collectionSize
        } else
            rem(collectionSize)
    }