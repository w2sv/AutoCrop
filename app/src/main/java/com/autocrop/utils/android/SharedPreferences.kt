package com.autocrop.utils.android

import android.app.Activity
import timber.log.Timber


private const val PREFERENCES_INSTANCE_NAME: String = "autocrop_preferences"


fun Activity.getSharedPreferencesBool(key: String, defaultValue: Boolean): Boolean = getSharedPreferences(
    PREFERENCES_INSTANCE_NAME,
    0
).getBoolean(key, defaultValue)


/**
 * Logs value to have been written to key
 */
fun Activity.writeSharedPreferencesBool(key: String, value: Boolean){
    getSharedPreferences(PREFERENCES_INSTANCE_NAME, 0)
        .edit().putBoolean(
            key,
            value
        )
        .apply()
        .also { Timber.i("Wrote $value to sharedPreferences.$key") }
}