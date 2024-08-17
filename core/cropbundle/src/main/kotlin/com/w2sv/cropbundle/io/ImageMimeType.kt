package com.w2sv.cropbundle.io

import android.graphics.Bitmap

const val IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER = "image/*"

enum class ImageMimeType(val outFileExtension: String, val compressFormat: Bitmap.CompressFormat) {
    JPG("jpg", Bitmap.CompressFormat.JPEG),
    PNG("png", Bitmap.CompressFormat.PNG),

    @Suppress("DEPRECATION")
    WEBP("webp", Bitmap.CompressFormat.WEBP);

    val mediaStoreIdentifier: String = "image/$outFileExtension"

    companion object {
        fun parse(mediaStoreIdentifier: String): ImageMimeType =
            when (mediaStoreIdentifier) {
                PNG.mediaStoreIdentifier -> PNG
                WEBP.mediaStoreIdentifier -> WEBP
                else -> JPG  // may be "image/jpg" OR "image/jpeg"
            }
    }
}