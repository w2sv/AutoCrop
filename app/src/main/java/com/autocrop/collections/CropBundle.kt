package com.autocrop.collections

import android.graphics.Bitmap
import android.net.Uri
import com.autocrop.utilsandroid.approximateJpegSize
import kotlin.math.roundToInt

/**
 * Encapsulation of entirety of data being associated with crop
 */
data class CropBundle(val screenshotParameters: ScreenshotParameters, var crop: Crop) {

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
        (_discardedPercentage() * screenshotParameters.approximateJpegSize).roundToInt()

    private fun _discardedPercentage(): Float =
        (screenshotParameters.height - crop.bitmap.height).toFloat() / screenshotParameters.height.toFloat()

    /**
     * @return [hashCode] of [screenshotParameters].uri, since per definition in and itself unambiguous
     */
    override fun hashCode(): Int = screenshotParameters.uri.hashCode()
    override fun equals(other: Any?): Boolean =
        when{
            (this === other) -> true
            (javaClass != other?.javaClass) -> false
            else -> screenshotParameters.uri == (other as CropBundle).screenshotParameters.uri
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