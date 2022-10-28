package com.w2sv.autocrop.utils.android.extensions

import android.graphics.Bitmap
import java.io.OutputStream

fun Bitmap.compressToStream(stream: OutputStream, compressFormat: Bitmap.CompressFormat): Boolean =
    compress(compressFormat, 100, stream)
        .also { stream.close() }