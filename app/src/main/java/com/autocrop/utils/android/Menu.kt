package com.autocrop.utils.android

import android.annotation.SuppressLint
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder

fun MenuItem.persistMenuAfterClick(context: Context): Boolean = run {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
    actionView = View(context)
    setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem): Boolean = false
        override fun onMenuItemActionCollapse(item: MenuItem): Boolean = false
    })
    false
}

/**
 * @see
 *      https://www.material.io/components/menus/android#dropdown-menus=
 *      https://stackoverflow.com/questions/15454995/popupmenu-with-icons
 */
@SuppressLint("RestrictedApi")
fun Menu.makeIconsVisible() = (this as MenuBuilder).setOptionalIconsVisible(true)