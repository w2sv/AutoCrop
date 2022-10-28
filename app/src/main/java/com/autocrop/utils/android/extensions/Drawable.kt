package com.autocrop.utils.android.extensions

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes

fun Drawable.setColor(context: Context, @ColorRes colorId: Int) {
    @Suppress("DEPRECATION")
    setColorFilter(context.getThemedColor(colorId), PorterDuff.Mode.SRC_IN)
}