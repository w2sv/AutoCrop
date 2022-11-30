package com.w2sv.autocrop.cropbundle

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import com.w2sv.autocrop.cropbundle.cropping.CropEdges
import com.w2sv.autocrop.cropbundle.cropping.cropped
import com.w2sv.autocrop.cropbundle.io.ImageMimeType
import com.w2sv.autocrop.cropbundle.io.extensions.queryMediaStoreData
import com.w2sv.kotlinutils.extensions.rounded
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class CropBundle(val screenshot: Screenshot, var crop: Crop) : Parcelable {
    companion object {
        const val EXTRA = "com.w2sv.autocrop.CROP_BUNDLE"

        fun assemble(screenshot: Screenshot, screenshotBitmap: Bitmap, edges: CropEdges): CropBundle =
            CropBundle(
                screenshot,
                Crop.fromScreenshot(
                    screenshotBitmap,
                    screenshot.mediaStoreData.diskUsage,
                    edges
                )
            )
    }

    fun identifier(): String =
        hashCode().toString()
}

@Parcelize
data class Screenshot(
    val uri: Uri,
    val height: Int,
    val cropEdgesCandidates: List<CropEdges>,
    val mediaStoreData: MediaStoreData
) : Parcelable {
    @Parcelize
    data class MediaStoreData(
        val diskUsage: Long,
        val fileName: String,
        val parsedMimeType: ImageMimeType,
        val id: Long
    ) : Parcelable {
        companion object {
            fun query(contentResolver: ContentResolver, uri: Uri): MediaStoreData =
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
                        MediaStoreData(
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
@Parcelize
data class Crop(
    val bitmap: Bitmap,
    val edges: CropEdges,
    val discardedPercentage: Int,
    val discardedKB: Long
) : Parcelable {

    @IgnoredOnParcel
    val discardedFileSizeFormatted: String by lazy {
        if (discardedKB >= 1000)
            "${(discardedKB.toFloat() / 1000).rounded(1)}mb"
        else
            "${discardedKB}kb"
    }

    companion object {
        fun fromScreenshot(screenshotBitmap: Bitmap, screenshotDiskUsage: Long, edges: CropEdges): Crop {
            val cropBitmap = screenshotBitmap.cropped(edges)
            val discardedPercentageF =
                ((screenshotBitmap.height - cropBitmap.height).toFloat() / screenshotBitmap.height.toFloat())

            return Crop(
                cropBitmap,
                edges,
                (discardedPercentageF * 100).roundToInt(),
                (discardedPercentageF * screenshotDiskUsage / 1000).roundToInt().toLong()
            )
        }
    }
}