package com.autocrop.utilsandroid

import android.content.Context
import androidx.annotation.ColorRes

/**
 * Convenience
 */
fun Context.getThemedColor(@ColorRes id: Int): Int =
    resources.getColor(id, theme)