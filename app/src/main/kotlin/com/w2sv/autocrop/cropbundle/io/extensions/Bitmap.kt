package com.w2sv.autocrop.cropbundle.io.extensions

import android.graphics.Bitmap
import java.io.OutputStream

fun Bitmap.compressToAndCloseStream(stream: OutputStream, compressFormat: Bitmap.CompressFormat): Boolean =
    compress(compressFormat, 100, stream)
        .also { stream.close() }