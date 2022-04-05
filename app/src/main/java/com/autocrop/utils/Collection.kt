package com.autocrop.utils

import kotlin.math.abs

/**
 * Enables python-style indexing with signed integers
 */
fun <T> Collection<T>.at(index: Int): T =
    elementAt(index.run { if (this < 0) plus(size) else this })

typealias Index = Int

fun Index.rotated(distance: Int, collectionSize: Int): Int =
    plus(distance).run {
        if (this <= 0)
            (collectionSize - abs(this) % collectionSize) % collectionSize
        else
            rem(collectionSize)
    }