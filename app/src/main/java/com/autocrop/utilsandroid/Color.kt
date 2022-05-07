package com.autocrop.utilsandroid

import android.content.Context
import androidx.annotation.ColorRes

/**
 * Convenience fun
 */
fun getColorInt(@ColorRes id: Int, context: Context): Int =
    context.resources.getColor(id, context.theme)