package com.autocrop.utils.android

import com.w2sv.autocrop.BuildConfig


fun intentExtraIdentifier(name: String): String = "com.autocrop.$name"

fun debuggingModeEnabled(): Boolean = BuildConfig.DEBUG