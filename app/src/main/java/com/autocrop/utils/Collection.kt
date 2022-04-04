package com.autocrop.utils

/**
 * Enables python-style negative indexing
 */
fun <T> Collection<T>.at(index: Int): T = elementAt(index.run { if (this < 0) plus(size) else this })