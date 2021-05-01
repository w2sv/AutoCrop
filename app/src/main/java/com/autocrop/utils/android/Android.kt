package com.autocrop.utils.android

import android.app.Activity
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.bunsenbrenner.screenshotboundremoval.BuildConfig


fun intentExtraIdentifier(name: String): String = "com.autocrop.$name"


// -----------------Build Version Checking-----------------

fun debuggingMode(): Boolean = BuildConfig.DEBUG
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

fun Activity.persistMenuAfterItemClick(item: MenuItem): Boolean = item.run {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
    actionView = View(this@persistMenuAfterItemClick)
    setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
            return false
        }

        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
            return false
        }
    })

    false
}


fun screenResolution(windowManager: WindowManager): Point = Point().apply {
    windowManager.defaultDisplay.getRealSize(this)
}


fun View.show(){
    visibility = View.VISIBLE
}


fun View.hide(){
    visibility = View.GONE
}