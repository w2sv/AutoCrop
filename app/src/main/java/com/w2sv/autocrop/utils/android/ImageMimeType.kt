package com.w2sv.autocrop.utils.android

import android.graphics.Bitmap

val IMAGE_MIME_TYPE = imageMimeTypeString("*")

enum class ImageMimeType(val fileExtension: String, val compressFormat: Bitmap.CompressFormat) {
    JPG("jpg", Bitmap.CompressFormat.JPEG),
    PNG("png", Bitmap.CompressFormat.PNG),

    @Suppress("DEPRECATION")
    WEBP("webp", Bitmap.CompressFormat.WEBP);

    val string = imageMimeTypeString(fileExtension)

    companion object {
        fun parse(type: String): ImageMimeType =
            when (type) {
                JPG.string, imageMimeTypeString("jpeg") -> JPG
                PNG.string -> PNG
                WEBP.string -> WEBP
                else -> JPG
            }
    }
}

private fun imageMimeTypeString(suffix: String): String =
    "image/$suffix"