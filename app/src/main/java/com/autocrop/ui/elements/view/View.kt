package com.autocrop.ui.elements.view

import android.view.View
import com.autocrop.utils.kotlin.BlankFun

fun View.show() { visibility = View.VISIBLE }
fun View.remove() { visibility = View.GONE }

inline fun View.ifNotInEditMode(f: BlankFun){
    if (!isInEditMode)
        f()
}