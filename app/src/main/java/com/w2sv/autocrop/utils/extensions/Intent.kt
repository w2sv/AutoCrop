package com.w2sv.autocrop.utils.extensions

import android.content.Intent
import android.os.Build
import android.os.Parcelable

inline fun <reified T : Parcelable> Intent.getParcelable(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelableExtra(name, T::class.java)
    else
        @Suppress("DEPRECATION")
        getParcelableExtra(name)

inline fun <reified T : Parcelable> Intent.getParcelableArrayList(name: String): ArrayList<T>? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelableArrayListExtra(name, T::class.java)
    else
        @Suppress("DEPRECATION")
        getParcelableArrayListExtra(name)

fun Intent.getInt(name: String): Int =
    getIntExtra(name, -1)