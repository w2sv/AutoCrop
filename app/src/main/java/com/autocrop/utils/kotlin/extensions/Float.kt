package com.autocrop.utils.kotlin.extensions

import java.util.Locale

fun Float.rounded(nDecimalPlaces: Int): String =
    "%.${nDecimalPlaces}f".format(this, Locale.ENGLISH)