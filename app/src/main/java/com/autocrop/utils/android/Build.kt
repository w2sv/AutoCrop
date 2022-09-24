package com.autocrop.utils.android

import android.os.Build

val buildVersionNotNewerThanQ: Boolean =
    Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q