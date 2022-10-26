package com.autocrop.utils.android.extensions

import android.content.res.Resources
import androidx.annotation.IntegerRes

fun Resources.getLong(@IntegerRes id: Int): Long =
    getInteger(id).toLong()