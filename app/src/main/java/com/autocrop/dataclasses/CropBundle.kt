package com.autocrop.dataclasses

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import com.autocrop.activities.cropping.cropping.CropEdges
import com.autocrop.activities.cropping.cropping.cropped
import com.autocrop.utils.android.ImageMimeType
import com.autocrop.utils.android.extensions.queryMediaStoreColumns
import com.lyrebirdstudio.croppylib.utils.extensions.rounded
import kotlin.math.roundToInt

/**
 * Encapsulation of data associated with crop
 */
data class CropBundle(val screenshot: Screenshot, var crop: Crop) {
    companion object{
        fun assemble(screenshot: Screenshot, screenshotBitmap: Bitmap, edges: CropEdges): CropBundle =
            CropBundle(
                screenshot,
                Crop.fromScreenshot(
                    screenshotBitmap,
                    screenshot.diskUsage,
                    edges
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
    val cropEdgePairCandidates: List<CropEdges>){

    companion object{
        fun fromContentResolver(contentResolver: ContentResolver, uri: Uri, cropEdgePairCandidates: List<CropEdges>): Screenshot{
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
    val edges: CropEdges,
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
        fun fromScreenshot(screenshotBitmap: Bitmap, screenshotDiskUsage: Long, edges: CropEdges): Crop{
            val cropBitmap = screenshotBitmap.cropped(edges)
            val discardedPercentageF = ((screenshotBitmap.height - cropBitmap.height).toFloat() / screenshotBitmap.height.toFloat())

            return Crop(
                cropBitmap,
                edges,
                (discardedPercentageF * 100).roundToInt(),
                (discardedPercentageF * screenshotDiskUsage / 1000).roundToInt().toLong(),
                screenshotBitmap.height - edges.bottom
            )
        }
    }
}