package com.autocrop.utils.android

import android.os.Build

val apiNotNewerThanQ: Boolean
    get() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q