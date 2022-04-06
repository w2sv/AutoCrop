package com.autocrop.utils

import kotlin.math.abs

typealias Index = Int

fun Index.rotated(distance: Int, collectionSize: Int): Int =
    plus(distance).run {
        if (this <= 0)
            (collectionSize - abs(this) % collectionSize) % collectionSize
        else
            rem(collectionSize)
    }