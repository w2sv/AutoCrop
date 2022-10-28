package com.autocrop.utils.android

import android.os.Handler
import android.os.Looper

fun postDelayed(delay: Long, runnable: Runnable): Boolean =
    Handler(Looper.getMainLooper())
        .postDelayed(
            runnable,
            delay
        )