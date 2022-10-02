package com.autocrop.utils.kotlin

import java.text.SimpleDateFormat
import java.util.*

fun dateTimeNow(): String =
    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())