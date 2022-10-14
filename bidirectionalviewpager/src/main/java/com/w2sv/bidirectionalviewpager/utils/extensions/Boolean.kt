package com.w2sv.bidirectionalviewpager.utils.extensions

fun Boolean.toInt(): Int =
    if (equals(true)) 1 else 0

fun Boolean.toNonZeroInt(): Int =
    if (equals(true)) 1 else -1
