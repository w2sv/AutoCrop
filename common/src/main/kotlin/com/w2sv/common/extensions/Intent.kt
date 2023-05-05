package com.w2sv.common.extensions

import android.content.Intent

fun Intent.getInt(name: String, defaultValue: Int = -1): Int =
    getIntExtra(name, defaultValue)