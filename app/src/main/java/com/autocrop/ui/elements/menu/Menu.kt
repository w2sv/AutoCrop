package com.autocrop.ui.elements.menu

import android.annotation.SuppressLint
import android.view.Menu
import androidx.appcompat.view.menu.MenuBuilder

/**
 * @see
 *      https://www.material.io/components/menus/android#dropdown-menus=
 *      https://stackoverflow.com/questions/15454995/popupmenu-with-icons
 */
@SuppressLint("RestrictedApi")
fun Menu.makeIconsVisible() =
    (this as MenuBuilder).setOptionalIconsVisible(true)