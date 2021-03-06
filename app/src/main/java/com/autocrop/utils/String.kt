package com.autocrop.utils

import java.util.*

fun String.numericallyInflected(quantity: Int): String =
    run{
        if (quantity > 1)
            plus("s")
        else
            this
    }

/**
 * @see
 *  https://stackoverflow.com/a/67843987/12083276
 */
fun String.capitalized(): String =
    replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else
            it.toString()
    }