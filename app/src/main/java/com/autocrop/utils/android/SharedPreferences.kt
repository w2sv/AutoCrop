package com.autocrop.utils.android

import android.app.Activity
import timber.log.Timber


enum class SharedPreferencesKey{
    DELETE_SCREENSHOTS,
    SAVE_TO_AUTOCROP_DIR
}


private const val PREFERENCES_INSTANCE_NAME: String = "autocrop_preferences"


fun Activity.getSharedPreferencesBool(key: SharedPreferencesKey, defaultValue: Boolean): Boolean = getSharedPreferences(
    PREFERENCES_INSTANCE_NAME,
    0
).getBoolean(key.name, defaultValue)


/**
 * Logs value to have been written to key
 */
fun Activity.writeSharedPreferencesBool(key: SharedPreferencesKey, value: Boolean){
    getSharedPreferences(PREFERENCES_INSTANCE_NAME, 0)
        .edit().putBoolean(
            key.name,
            value
        )
        .apply()
        .also { Timber.i("Wrote $value to sharedPreferences.${key.name}") }
}