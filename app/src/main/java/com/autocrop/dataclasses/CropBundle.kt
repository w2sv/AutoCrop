package com.autocrop.dataclasses

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.autocrop.activities.cropping.cropping.cropped
import com.autocrop.utils.android.ImageMimeType
import com.autocrop.utils.android.extensions.queryMediaStoreData
import com.lyrebirdstudio.croppylib.CropEdges
import com.lyrebirdstudio.croppylib.utils.extensions.rounded
import kotlin.math.roundToInt

data class CropBundle(val screenshot: Screenshot, var crop: Crop) {
    companion object{
        fun assemble(screenshot: Screenshot, screenshotBitmap: Bitmap, edges: CropEdges): CropBundle =
            CropBundle(
                screenshot,
                Crop.fromScreenshot(
                    screenshotBitmap,
                    screenshot.mediaStoreColumns.diskUsage,
                    edges
                )
            )
    }

    fun identifier(): String = hashCode().toString()
}

data class Screenshot(
    val uri: Uri,
    val height: Int,
    val cropEdgesCandidates: List<CropEdges>,
    val mediaStoreColumns: MediaStoreColumns){

    data class MediaStoreColumns(val diskUsage: Long,
                                 val fileName: String,
                                 val parsedMimeType: ImageMimeType,
                                 val id: Long){
        companion object{
            fun query(contentResolver: ContentResolver, uri: Uri): MediaStoreColumns =
                contentResolver.queryMediaStoreData(
                    uri,
                    arrayOf(
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media._ID
                    )
                )
                    .run {
                        MediaStoreColumns(
                            get(0).toLong(),
                            get(1),
                            ImageMimeType.parse(get(2)),
                            get(3).toLong()
                        )
                    }
        }
    }
}

// TODO: write tests
data class Crop(
    val bitmap: Bitmap,
    val edges: CropEdges,
    val discardedPercentage: Int,
    val discardedKB: Long) {

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
                (discardedPercentageF * screenshotDiskUsage / 1000).roundToInt().toLong()
            )
        }
    }
}