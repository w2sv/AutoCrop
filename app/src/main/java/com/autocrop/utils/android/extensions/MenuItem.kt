package com.autocrop.utils.android.extensions

import android.content.Context
import android.view.MenuItem
import android.widget.Switch
import com.autocrop.preferences.BooleanPreferences

fun MenuItem.setBooleanPreferencesManagedSwitch(context: Context, booleanPreferencesKey: String){
    actionView = Switch(context).apply {
        isChecked = BooleanPreferences.getValue(booleanPreferencesKey)
        setOnCheckedChangeListener { _, isChecked ->
            BooleanPreferences[booleanPreferencesKey] = isChecked
        }
    }
}