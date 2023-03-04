package com.w2sv.common.extensions

import android.os.Bundle
import android.os.Parcelable

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(name: String): T? =
    getParcelable(name) as? T