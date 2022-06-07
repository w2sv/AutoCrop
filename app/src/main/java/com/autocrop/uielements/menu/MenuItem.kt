package com.autocrop.uielements.menu

import android.content.Context
import android.view.MenuItem
import android.view.View

fun MenuItem.persistMenuAfterClick(context: Context): MenuItem =
    run {
        setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        actionView = View(context)
        setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = false
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean = false
        })
    }