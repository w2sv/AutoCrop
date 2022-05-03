package com.autocrop.collections

import android.graphics.Bitmap
import android.net.Uri

/**
 * Encapsulation of entirety of data being associated with crop
 */
data class CropBundle(
    val screenshotUri: Uri,
    val crop: Bitmap,
    val discardedPercentage: Int,
    val approximateDiscardedFileSize: Int) {

    /**
     * @return [hashCode] of [screenshotUri], since per definition in and itself unambiguous
     */
    override fun hashCode(): Int = screenshotUri.hashCode()
    override fun equals(other: Any?): Boolean =
        when{
            (this === other) -> true
            (javaClass != other?.javaClass) -> false
            else -> screenshotUri == (other as CropBundle).screenshotUri
        }
}