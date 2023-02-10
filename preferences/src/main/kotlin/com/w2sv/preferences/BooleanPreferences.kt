package com.w2sv.preferences

import android.content.Context
import android.content.SharedPreferences
import android.widget.Switch
import com.w2sv.typedpreferences.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KMutableProperty0

@Singleton
class BooleanPreferences @Inject constructor(appPreferences: SharedPreferences) : BooleanPreferences(
    "autoScroll" to true,
    "deleteScreenshots" to false,
    sharedPreferences = appPreferences
) {
    var autoScroll by this
    var deleteScreenshots by this
}

fun KMutableProperty0<Boolean>.getConnectedSwitch(context: Context): Switch =
    Switch(context).apply {
        isChecked = get()
        setOnCheckedChangeListener { _, isChecked ->
            set(isChecked)
        }
    }