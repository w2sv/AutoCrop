package com.autocrop.utils.android.extensions

import android.content.Intent
import android.os.Build
import android.os.Parcelable

inline fun <reified T: Parcelable> Intent.getParcelable(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelableExtra(name, T::class.java)
    else
        @Suppress("DEPRECATION")
        getParcelableExtra(name) as T?

fun Intent.getInt(name: String): Int =
    getIntExtra(name, -1)