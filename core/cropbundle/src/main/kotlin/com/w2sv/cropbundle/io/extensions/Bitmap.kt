package com.w2sv.cropbundle.io.extensions

import android.graphics.Bitmap
import androidx.annotation.IntRange
import java.io.OutputStream

fun Bitmap.compressToAndCloseStream(
    stream: OutputStream,
    compressFormat: Bitmap.CompressFormat,
    @IntRange(0, 100) quality: Int = 100
): Boolean =
    compress(compressFormat, quality, stream)
        .also { stream.close() }