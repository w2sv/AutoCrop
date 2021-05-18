package com.autocrop.utils

fun <T> Collection<T>.at(index: Int): T =
    if (index < 0)
        elementAt(size + index)
    else
        elementAt(index)

fun <T> Collection<T>.getByBoolean(flag: Boolean): T = elementAt(flag.toInt())