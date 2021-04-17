package com.autocrop.utils.android

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast


fun intentExtraIdentifier(name: String): String = "com.autocrop.$name"


// -----------------Build Version Checking-----------------

fun apiLowerEquals(apiNumber: Int): Boolean = Build.VERSION.SDK_INT <= apiNumber


// -----------------Message Display-----------------

fun Activity.displayToast(vararg row: String) {
    Toast.makeText(
        this,
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


fun paddedMessage(vararg row: String): String = " ${row.joinToString(" \n ")} "


// -----------------Menu Persisting-----------------

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