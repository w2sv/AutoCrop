package com.autocrop.utils.android

import android.app.Activity
import android.view.MenuItem
import android.view.View


fun Activity.persistMenuAfterItemClick(item: MenuItem): Boolean = item.run {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
    actionView = View(this@persistMenuAfterItemClick)
    setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem): Boolean = false
        override fun onMenuItemActionCollapse(item: MenuItem): Boolean = false
    })
    false
}