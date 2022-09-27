package com.autocrop.utils.android

import android.os.Environment
import java.io.File

@Suppress("DEPRECATION")
val externalPicturesDir: File =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)