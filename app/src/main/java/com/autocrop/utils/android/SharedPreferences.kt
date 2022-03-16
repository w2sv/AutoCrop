package com.autocrop.utils.android

import android.app.Activity
import android.content.SharedPreferences


fun Activity.getDefaultSharedPreferences(): SharedPreferences = getSharedPreferences(
    "default",
    0
)


fun SharedPreferences.writeBoolean(key: String, value: Boolean) =
    edit().putBoolean(
        key,
        value
    )
        .apply()