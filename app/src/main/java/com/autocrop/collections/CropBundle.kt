package com.autocrop.collections

import android.graphics.Bitmap
import android.net.Uri
import com.autocrop.utilsandroid.approximateJpegSize
import kotlin.math.roundToInt

/**
 * Encapsulation of entirety of data being associated with crop
 */
data class CropBundle(val screenshot: ScreenshotParameters, var crop: Crop) {

    constructor(screenshotUri: Uri, screenshot: Bitmap, cropTopEdge: Int, cropHeight: Int)
        : this(
            ScreenshotParameters(
                screenshotUri,
                screenshot.height,
                screenshot.approximateJpegSize()
            ),
            Crop(
                Bitmap.createBitmap(screenshot,0, cropTopEdge, screenshot.width, cropHeight),
                screenshot.height - cropTopEdge - cropHeight,
                cropTopEdge
            )
        )

    fun discardedPercentage(): Int =
        (_discardedPercentage() * 100).roundToInt()

    fun discardedFileSize(): Int =
        (_discardedPercentage() * screenshot.approximateJpegSize).roundToInt()

    private fun _discardedPercentage(): Float =
        (screenshot.height - crop.bitmap.height).toFloat() / screenshot.height.toFloat()

    /**
     * @return [hashCode] of [screenshot].uri, since per definition in and itself unambiguous
     */
    override fun hashCode(): Int = screenshot.uri.hashCode()
    override fun equals(other: Any?): Boolean =
        when{
            (this === other) -> true
            (javaClass != other?.javaClass) -> false
            else -> screenshot.uri == (other as CropBundle).screenshot.uri
        }
}

data class ScreenshotParameters(
    val uri: Uri,
    val height: Int,
    val approximateJpegSize: Int
)

data class Crop(
    val bitmap: Bitmap,
    val bottomOffset: Int,
    val topOffset: Int
)