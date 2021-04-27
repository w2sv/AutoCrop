package com.autocrop.utils.android

import android.app.Activity
import android.content.SharedPreferences


private const val DEFAULT_PREFERENCES_INSTANCE_NAME: String = "default"


fun Activity.getDefaultSharedPreferences(): SharedPreferences = getSharedPreferences(
    DEFAULT_PREFERENCES_INSTANCE_NAME,
    0
)


fun SharedPreferences.writeBoolean(key: String, value: Boolean){
    this.edit().putBoolean(
        key,
        value
    )
        .apply()
}