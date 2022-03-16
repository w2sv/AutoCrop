package com.autocrop.utils

/**
 * Enables python-style negative indexing
 */
fun <T> Collection<T>.at(index: Int): T = elementAt(index + listOf(0, size)[index < 0])

/**
 * Enables Collection indexing bmo Boolean
 */
operator fun <T> Collection<T>.get(flag: Boolean): T = elementAt(flag.toInt())