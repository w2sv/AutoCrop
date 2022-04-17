package com.autocrop.utils

import java.util.*

fun numberInflection(quantity: Int): String = if (quantity > 1) "s" else ""

/**
 * @see
 *  https://stackoverflow.com/a/67843987/12083276
 */
fun String.capitalized(): String {
    return replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else
            it.toString()
    }
}