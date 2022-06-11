package com.lyrebirdstudio.croppylib.utils.extensions

import android.view.View

fun View.show() {
    visibility = View.VISIBLE
}

fun View.remove() {
    visibility = View.GONE
}