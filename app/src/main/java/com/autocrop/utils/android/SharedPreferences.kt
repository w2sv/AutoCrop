package com.autocrop.utils.android

import android.content.Context
import android.content.SharedPreferences

fun Context.getSharedPreferences(): SharedPreferences =
    getSharedPreferences(packageName, Context.MODE_PRIVATE)