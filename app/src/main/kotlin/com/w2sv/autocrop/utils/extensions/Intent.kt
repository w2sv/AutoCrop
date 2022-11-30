package com.w2sv.autocrop.utils.extensions

import android.content.Intent
import android.os.Parcelable

/**
 * Convenience function to be used as long as no Compat function available for
 * getParcelableExtra(@Nullable String name, @NonNull Class<T> clazz)
 * and the function remains bugged.
 *
 * Issue tracker links:
 *      https://issuetracker.google.com/issues/242048899
 *      https://issuetracker.google.com/issues/240585930
 */
inline fun <reified T : Parcelable> Intent.getParcelable(name: String): T? =
    @Suppress("DEPRECATION")
    getParcelableExtra(name)

fun Intent.getInt(name: String, defaultValue: Int = -1): Int =
    getIntExtra(name, defaultValue)