package com.autocrop.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.autocrop.GlobalParameters
import com.autocrop.PREFERENCES_INSTANCE_NAME
import com.autocrop.PreferencesKey


fun paddedMessage(vararg row: String): String = " ${row.joinToString(" \n ")} "


fun Activity.displayToast(vararg row: String) {
    Toast.makeText(
        this@displayToast,
        paddedMessage(*row),
        Toast.LENGTH_LONG
    ).apply{
        this.view!!.setBackgroundColor(Color.parseColor("darkgray"))
        (this.view.findViewById<View>(android.R.id.message) as TextView).apply {
            this.setTextColor(Color.parseColor("white"))
        }

        this.show()
    }
}


fun Activity.persistMenuAfterItemClick(item: MenuItem): Boolean{
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
    item.setActionView(View(this))
    item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
            return false
        }

        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
            return false
        }
    })

    return false
}


fun Activity.getSharedPreferencesBool(key: PreferencesKey, defaultValue: Boolean): Boolean = getSharedPreferences(
    PREFERENCES_INSTANCE_NAME,
    0
).getBoolean(key.name, defaultValue)


fun Activity.writeSharedPreferencesBool(key: PreferencesKey, value: Boolean){
    getSharedPreferences(PREFERENCES_INSTANCE_NAME, 0)
        .edit().putBoolean(
            key.name,
            value
        )
        .apply()
}


fun apiLowerEquals(apiNumber: Int): Boolean = Build.VERSION.SDK_INT <= apiNumber