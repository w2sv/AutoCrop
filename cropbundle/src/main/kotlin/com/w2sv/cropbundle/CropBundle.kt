package com.w2sv.cropbundle

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import com.w2sv.cropbundle.cropping.CropEdges
import com.w2sv.cropbundle.cropping.Cropper
import com.w2sv.cropbundle.cropping.cropped
import com.w2sv.cropbundle.io.ImageMimeType
import com.w2sv.cropbundle.io.extensions.loadBitmap
import com.w2sv.cropbundle.io.extensions.queryMediaStoreData
import com.w2sv.kotlinutils.extensions.rounded
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class CropBundle(val screenshot: Screenshot, var crop: Crop) : Parcelable {

    enum class CreationFailureReason {
        BitmapLoadingFailure,
        NoCropEdgesFound
    }

    companion object {
        const val EXTRA_POSITION = "com.w2sv.autocrop.extra.CROP_BUNDLE_POSITION"

        fun attemptCreation(
            screenshotMediaUri: Uri,
            context: Context
        ): Pair<CropBundle?, CreationFailureReason?> =
            context.contentResolver.loadBitmap(screenshotMediaUri)?.let { screenshotBitmap ->
                Cropper.invoke(screenshotBitmap, context)?.let { (edges, candidates) ->
                    val screenshot = Screenshot(
                        screenshotMediaUri,
                        screenshotBitmap.height,
                        candidates,
                        Screenshot.MediaStoreData.query(context.contentResolver, screenshotMediaUri)
                    )

                    Pair(
                        CropBundle(
                            screenshot = screenshot,
                            crop = Crop.fromScreenshot(
                                screenshotBitmap,
                                screenshot.mediaStoreData.diskUsage,
                                edges
                            )
                        ),
                        null
                    )
                }
                    ?: Pair(null, CreationFailureReason.NoCropEdgesFound)
            }
                ?: Pair(null, CreationFailureReason.BitmapLoadingFailure)
    }

    fun identifier(): String =
        hashCode().toString()
}

@Parcelize
data class Screenshot(
    val uri: Uri,
    val height: Int,
    val cropEdgeCandidates: List<Int>,
    val mediaStoreData: MediaStoreData
) : Parcelable {

    @Parcelize
    data class MediaStoreData(
        val diskUsage: Long,
        val fileName: String,
        val mimeType: ImageMimeType,
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

    fun getBitmap(contentResolver: ContentResolver): Bitmap =
        contentResolver.loadBitmap(uri)!!
}

// TODO: write tests
@Parcelize
data class Crop(
    val bitmap: Bitmap,
    val edges: CropEdges,
    val discardedPercentage: Int,
    private val discardedKB: Long
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