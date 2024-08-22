package com.w2sv.cropbundle

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import com.w2sv.cropbundle.cropping.model.CropEdges
import com.w2sv.cropbundle.cropping.CropSensitivity
import com.w2sv.cropbundle.cropping.crop
import com.w2sv.cropbundle.cropping.cropped
import com.w2sv.cropbundle.io.ImageMimeType
import com.w2sv.cropbundle.io.extensions.loadBitmap
import com.w2sv.cropbundle.io.extensions.queryMediaStoreData
import com.w2sv.kotlinutils.rounded
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import slimber.log.i
import kotlin.math.roundToInt

@Parcelize
data class CropBundle(
    val screenshot: Screenshot,
    var crop: Crop,  // TODO: vars
    var edgeCandidates: List<Int>,
    @CropSensitivity var cropSensitivity: Int
) : Parcelable {

    sealed interface CreationResult {
        @JvmInline
        value class Success(val cropBundle: CropBundle) : CreationResult

        sealed interface Failure : CreationResult {
            data object NoCropEdgesFound : Failure
            data object BitmapLoadingFailed : Failure
        }
    }

    val identifier: String
        get() = hashCode().toString()

    companion object {
        const val EXTRA_POSITION = "com.w2sv.autocrop.extra.CROP_BUNDLE_POSITION"

        fun attemptCreation(
            screenshotMediaUri: Uri,
            @CropSensitivity cropSensitivity: Int,
            contentResolver: ContentResolver
        ): CreationResult =
            when (val screenshotBitmap = contentResolver.loadBitmap(screenshotMediaUri)) {
                null -> CreationResult.Failure.BitmapLoadingFailed
                else -> {
                    when (val cropResult = screenshotBitmap.crop(cropSensitivity)) {
                        null -> CreationResult.Failure.NoCropEdgesFound
                        else -> {
                            val screenshot = Screenshot(
                                uri = screenshotMediaUri,
                                height = screenshotBitmap.height,
                                mediaStoreData = Screenshot.MediaStoreData.query(
                                    contentResolver,
                                    screenshotMediaUri
                                )
                            )
                            CreationResult.Success(
                                cropBundle = CropBundle(
                                    screenshot = screenshot,
                                    crop = Crop.fromScreenshot(
                                        screenshotBitmap = screenshotBitmap,
                                        screenshotDiskUsage = screenshot.mediaStoreData.diskUsage,
                                        edges = cropResult.edges
                                    ),
                                    edgeCandidates = cropResult.candidates,
                                    cropSensitivity = cropSensitivity
                                )
                            )
                        }
                    }
                }
            }
    }
}

@Parcelize
data class Screenshot(
    val uri: Uri,
    val height: Int,
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
            fun query(contentResolver: ContentResolver, uri: Uri): MediaStoreData {
                i { "uri: $uri" }  // content://media/picker/0/com.android.providers.media.photopicker/media/1000016069
                return contentResolver.queryMediaStoreData(
                    uri = uri,
                    columns = arrayOf(
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media._ID
                    ),
                    onCursor = {
                        val fileName = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                        MediaStoreData(
                            diskUsage = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)),
                            fileName = fileName,
                            mimeType = ImageMimeType.parse(it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))),
                            id = it.getLongOrNull(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                                ?: fileName.substringBeforeLast(".").toLong()  // TODO: probably still unreliable
                        )
                            .also { i { it.toString() } }
                    }
                )!!
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
                bitmap = cropBitmap,
                edges = edges,
                discardedPercentage = (discardedPercentageF * 100).roundToInt(),
                discardedKB = (discardedPercentageF * screenshotDiskUsage / 1000).roundToInt().toLong()
            )
        }
    }
}