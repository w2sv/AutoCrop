package com.autocrop.utils.android

import android.graphics.Bitmap

const val IMAGE_MIME_TYPE = "image/*"

enum class ImageMimeType(val string: String, val compressFormat: Bitmap.CompressFormat){
    JPEG("image/jpeg", Bitmap.CompressFormat.JPEG),
    PNG("image/png", Bitmap.CompressFormat.PNG),
    @Suppress("DEPRECATION")
    WEBP("image/webp", Bitmap.CompressFormat.WEBP);

    companion object{
        fun parse(type: String): ImageMimeType =
            when(type){
                "image/jpeg" -> JPEG
                "image/png" -> PNG
                "image/webp" -> WEBP
                else -> JPEG
            }
    }
}