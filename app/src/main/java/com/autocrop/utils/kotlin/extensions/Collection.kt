package com.autocrop.utils.kotlin.extensions

fun <T> Collection<T>.rotatedIndex(index: Int, distance: Int): Int =
    (index + distance).let {
        if (it < 0)
            size + it
        else
            it
    } % size