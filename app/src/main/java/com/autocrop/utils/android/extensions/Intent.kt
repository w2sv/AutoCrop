package com.autocrop.utils.android.extensions

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import java.io.Serializable

inline fun <reified T: Parcelable> Intent.getParcelable(name: String): T =
    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelableExtra(name, T::class.java)
    else
        @Suppress("DEPRECATION")
        getParcelableExtra(name) as T?)!!

inline fun <reified T: Serializable> Intent.getSerializable(name: String): T =
    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getSerializableExtra(name, T::class.java)
    else
        @Suppress("DEPRECATION")
        getSerializableExtra(name) as T?)!!

fun Intent.getInt(name: String): Int =
    getIntExtra(name, -1)