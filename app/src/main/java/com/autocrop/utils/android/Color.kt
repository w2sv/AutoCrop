package com.autocrop.utils.android

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

/**
 * Convenience fun
 */
fun getColorInt(@ColorRes id: Int, context: Context): Int =
    context.resources.getColor(id, context.theme)