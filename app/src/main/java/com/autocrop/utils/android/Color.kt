package com.autocrop.utils.android

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

/**
 * @return precached [ColorInt] if present, otherwise caches before returning
 */
fun getColorInt(@ColorRes id: Int, context: Context): Int =
    id2Color.getOrPut(id){ context.resources.getColor(id, context.theme) }

private val id2Color: MutableMap<Int, Int> = mutableMapOf()