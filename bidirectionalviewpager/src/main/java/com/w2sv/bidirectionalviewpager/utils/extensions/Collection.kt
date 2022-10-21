package com.w2sv.bidirectionalviewpager.utils.extensions

fun <T> Collection<T>.rotatedIndex(index: Int, distance: Int): Int =
    (index + distance).let {
        if (it < 0)
            size + it
        else
            it
    } % size