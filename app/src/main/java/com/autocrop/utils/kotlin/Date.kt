package com.autocrop.utils.kotlin

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun dateTimeNow(): String =
    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())