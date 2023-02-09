package com.w2sv.common.utils.extensions

import android.content.Intent
import android.os.Parcelable

/**
 * Convenience function to be used as long as no Compat function available for
 * getParcelableExtra(@Nullable String name, @NonNull Class<T> clazz)
 * and the function remains bugged.
 *
 * Issue tracker links:
 *      https://issuetracker.google.com/issues/242048899 == more current
 *      https://issuetracker.google.com/issues/240585930
 */
@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? =
    getParcelableExtra(name) as? T

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(name: String): ArrayList<T>? =
    getParcelableArrayListExtra(name)

fun Intent.getInt(name: String, defaultValue: Int = -1): Int =
    getIntExtra(name, defaultValue)