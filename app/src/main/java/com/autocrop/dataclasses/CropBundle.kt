package com.autocrop.dataclasses

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import com.autocrop.activities.cropping.fragments.cropping.cropping.VerticalEdges
import com.autocrop.activities.cropping.fragments.cropping.cropping.cropped
import com.autocrop.utils.android.ImageMimeType
import com.autocrop.utils.android.extensions.queryMediaStoreColumns
import com.lyrebirdstudio.croppylib.utils.extensions.rounded
import kotlin.math.roundToInt

/**
 * Encapsulation of data associated with crop
 */
data class CropBundle(val screenshot: Screenshot, var crop: Crop) {
    companion object{
        fun assemble(screenshot: Screenshot, screenshotBitmap: Bitmap, cropRect: Rect): CropBundle =
            CropBundle(
                screenshot,
                Crop.fromScreenshot(
                    screenshotBitmap,
                    screenshot.diskUsage,
                    cropRect
                )
            )
    }

    fun identifier(): String = hashCode().toString()
}

data class Screenshot(
    val uri: Uri,
    val diskUsage: Long,
    val fileName: String,
    val parsedMimeType: ImageMimeType,
    val mediaStoreId: Long,
    val cropEdgePairCandidates: List<VerticalEdges>){

    companion object{
        fun fromContentResolver(contentResolver: ContentResolver, uri: Uri, cropEdgePairCandidates: List<VerticalEdges>): Screenshot{
            val mediaColumns = contentResolver.queryMediaStoreColumns(
                uri,
                arrayOf(
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media._ID
                )
            )

            return Screenshot(
                uri,
                mediaColumns[0].toLong(),
                mediaColumns[1],
                ImageMimeType.parse(mediaColumns[2]),
                mediaColumns[3].toLong(),
                cropEdgePairCandidates
            )
                .also { println("Parsed screenshot: $it") }
        }
    }

    fun bitmap(contentResolver: ContentResolver): Bitmap =
        BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
}

// TODO: write tests
data class Crop(
    val bitmap: Bitmap,
    val rect: Rect,
    val discardedPercentage: Int,
    val discardedKB: Long,
    val bottomOffset: Int) {

    val discardedFileSizeFormatted: String by lazy {
        if (discardedKB >= 1000)
            "${(discardedKB.toFloat() / 1000).rounded(1)}mb"
        else
            "${discardedKB}kb"
    }

    companion object{
        fun fromScreenshot(screenshotBitmap: Bitmap, screenshotDiskUsage: Long, rect: Rect): Crop{
            val cropBitmap = screenshotBitmap.cropped(rect)
            val discardedPercentageF = ((screenshotBitmap.height - cropBitmap.height).toFloat() / screenshotBitmap.height.toFloat())

            return Crop(
                cropBitmap,
                rect,
                (discardedPercentageF * 100).roundToInt(),
                (discardedPercentageF * screenshotDiskUsage / 1000).roundToInt().toLong(),
                screenshotBitmap.height - rect.bottom
            )
        }
    }
}