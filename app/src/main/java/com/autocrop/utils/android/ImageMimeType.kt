package com.autocrop.utils.android

import android.graphics.Bitmap

const val IMAGE_MIME_TYPE = "image/*"

enum class ImageMimeType(val fileExtension: String,val compressFormat: Bitmap.CompressFormat){
    JPEG("jpg", Bitmap.CompressFormat.JPEG),
    PNG("png", Bitmap.CompressFormat.PNG),
    @Suppress("DEPRECATION")
    WEBP("webp", Bitmap.CompressFormat.WEBP);

    val string: String get() = "image/$fileExtension"

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