package com.autocrop.utils.android

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences


fun Activity.getSharedPreferences(): SharedPreferences = getPreferences(Context.MODE_PRIVATE)

fun SharedPreferences.writeBoolean(key: String, value: Boolean) = edit()
    .putBoolean(key, value)
    .apply()