package com.autocrop.utils

fun Boolean.toInt(): Int =
    if (equals(true)) 1 else 0

fun Boolean.toNonZeroInt(): Int =
    if (equals(true)) 1 else -1
