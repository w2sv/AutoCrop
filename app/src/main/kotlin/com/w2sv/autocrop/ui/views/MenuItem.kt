package com.w2sv.autocrop.ui.views

import android.content.Context
import android.view.MenuItem
import android.view.View

fun MenuItem.makeOnClickPersistent(context: Context) {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
    actionView = View(context)
    setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem): Boolean =
            false

        override fun onMenuItemActionCollapse(item: MenuItem): Boolean =
            false
    })
}

fun MenuItem.toggleCheck(listener: (Boolean) -> Unit) {
    val newValue = !isChecked
    isChecked = newValue
    listener(newValue)
}

const val KEEP_MENU_ITEM_OPEN_ON_CLICK: Boolean = false