package com.autocrop.utils.android

import android.app.Activity



enum class SharedPreferencesKey{
    DELETE_SCREENSHOTS,
    SAVE_TO_AUTOCROP_FOLDER
}


const val PREFERENCES_INSTANCE_NAME: String = "autocrop_preferences"


fun Activity.getSharedPreferencesBool(key: SharedPreferencesKey, defaultValue: Boolean): Boolean = getSharedPreferences(
    PREFERENCES_INSTANCE_NAME,
    0
).getBoolean(key.name, defaultValue)


fun Activity.writeSharedPreferencesBool(key: SharedPreferencesKey, value: Boolean){
    getSharedPreferences(PREFERENCES_INSTANCE_NAME, 0)
        .edit().putBoolean(
            key.name,
            value
        )
        .apply()
}