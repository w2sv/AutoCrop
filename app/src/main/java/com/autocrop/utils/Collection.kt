package com.autocrop.utils

fun <T> Collection<T>.at(index: Int): T =
    if (index < 0)
        elementAt(size + index)
    else
        elementAt(index)

operator fun <T> Collection<T>.get(flag: Boolean): T = elementAt(flag.toInt())