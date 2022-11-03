package com.w2sv.autocrop.utils.android.extensions

import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes

inline fun Menu.configureItem(@IdRes id: Int, f: (MenuItem) -> Unit) {
    f(findItem(id))
}