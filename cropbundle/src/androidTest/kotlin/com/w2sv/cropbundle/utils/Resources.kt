package com.w2sv.cropbundle.utils

import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStream

fun assetFileStream(subPath: String): InputStream =
    InstrumentationRegistry
        .getInstrumentation()
        .context
        .resources
        .assets
        .open(subPath)