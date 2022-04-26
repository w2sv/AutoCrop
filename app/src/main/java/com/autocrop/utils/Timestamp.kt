package com.autocrop.utils

import java.text.SimpleDateFormat
import java.util.*

fun formattedDateTimeString(): String =
    Calendar.getInstance().time.toFormattedString()

private fun Date.toFormattedString(): String =
    SimpleDateFormat(
        "yyyy-MM-dd_HH:mm:ss.SSS",
        Locale.getDefault()
    ).format(this)