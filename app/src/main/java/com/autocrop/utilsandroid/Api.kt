package com.autocrop.utilsandroid

import android.os.Build

val apiNotNewerThanQ: Boolean =
    Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q