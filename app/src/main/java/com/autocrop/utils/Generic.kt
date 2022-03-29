package com.autocrop.utils


fun Boolean.toInt(): Int = compareTo(false)

fun Any?.notNull(): Boolean = this != null