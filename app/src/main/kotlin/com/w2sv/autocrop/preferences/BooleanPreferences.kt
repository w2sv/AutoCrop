package com.w2sv.autocrop.preferences

import android.content.Context
import android.content.SharedPreferences
import android.widget.Switch
import com.w2sv.typedpreferences.descendants.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooleanPreferences @Inject constructor(sharedPreferences: SharedPreferences) : BooleanPreferences(
    "autoScroll" to true,
    "deleteScreenshots" to false,
    sharedPreferences = sharedPreferences
) {
    var autoScroll by this
    var deleteScreenshots by this

    fun createSwitch(context: Context, key: String): Switch =
        Switch(context).apply {
            isChecked = getValue(key)
            setOnCheckedChangeListener { _, isChecked ->
                this@BooleanPreferences[key] = isChecked
            }
        }
}

