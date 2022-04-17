package com.autocrop.types

import android.graphics.Bitmap
import android.net.Uri

data class CropBundle(
    val screenshotUri: Uri,
    val crop: Bitmap,
    val discardedPercentage: Int,
    val approximateDiscardedFileSize: Int) {

    override fun hashCode(): Int = screenshotUri.hashCode()
    override fun equals(other: Any?): Boolean =
        when{
            (this === other) -> true
            (javaClass != other?.javaClass) -> false
            else -> screenshotUri == (other as CropBundle).screenshotUri
        }
}