package com.w2sv.autocrop.utils.kotlin

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun dateTimeNow(): String =
    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())

fun timeDelta(earlier: Date, later: Date, timeUnit: TimeUnit): Long =
    timeUnit.convert(
        later.time - earlier.time,
        TimeUnit.MILLISECONDS
    )

fun dateFromUnixTimestamp(unixTimestamp: String): Date =
    Date(unixTimestamp.toLong() * 1000)