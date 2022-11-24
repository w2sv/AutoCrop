package com.w2sv.autocrop.utils

import android.os.Build
import android.os.Environment
import java.io.File

fun systemPicturesDirectory(): File =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

fun systemScreenshotsDirectory(): File? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS)
    else
        null