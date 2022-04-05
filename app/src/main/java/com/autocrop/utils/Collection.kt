package com.autocrop.utils

/**
 * Enables python-style indexing with signed integers
 */
fun <T> Collection<T>.at(index: Int): T =
    elementAt(index.run { if (this < 0) plus(size) else this })