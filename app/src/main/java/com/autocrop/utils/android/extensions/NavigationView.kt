package com.autocrop.utils.android.extensions

import android.widget.Switch
import androidx.annotation.IdRes
import com.autocrop.preferences.BooleanPreferences
import com.google.android.material.navigation.NavigationView

fun NavigationView.setItemSwitch(@IdRes itemId: Int, booleanPreferencesKey: String, checkedChangeListener: ((Boolean) -> Unit)? = null){
    menu.findItem(itemId).actionView = Switch(context).apply {
        isChecked = BooleanPreferences.getValue(booleanPreferencesKey)
        setOnCheckedChangeListener { _, isChecked ->
            BooleanPreferences[booleanPreferencesKey] = isChecked
            checkedChangeListener?.invoke(isChecked)
        }
    }
}