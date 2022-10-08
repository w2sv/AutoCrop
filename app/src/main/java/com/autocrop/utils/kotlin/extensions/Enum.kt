package com.autocrop.utils.kotlin.extensions

val Enum<*>.nonZeroOrdinal: Int get() = ordinal + 1