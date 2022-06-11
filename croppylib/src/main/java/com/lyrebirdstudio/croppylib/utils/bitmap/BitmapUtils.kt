package com.lyrebirdstudio.croppylib.utils.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.lyrebirdstudio.croppylib.utils.extensions.rotateBitmap
import java.io.IOException
import java.io.InputStream

private const val MAX_SIZE = 1024

fun resizedBitmap(uri: Uri, context: Context): Bitmap{
    val options = BitmapFactory.Options()
        .apply {
            inJustDecodeBounds = true
        }

    BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)

    var widthTemp = options.outWidth
    var heightTemp = options.outHeight
    var scale = 1

    while (true) {
        if (widthTemp / 2 < MAX_SIZE || heightTemp / 2 < MAX_SIZE)
            break
        widthTemp /= 2
        heightTemp /= 2
        scale *= 2
    }

    return BitmapFactory.decodeStream(
        context.contentResolver.openInputStream(uri),
        null,
        BitmapFactory.Options().apply {
            inSampleSize = scale
        }
    )!!
        .rotateBitmap(getOrientation(context.contentResolver.openInputStream(uri)))
}

private fun getOrientation(inputStream: InputStream?): Int {
    val exifInterface: ExifInterface
    var orientation = 0
    try {
        exifInterface = ExifInterface(inputStream!!)
        orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return orientation
}