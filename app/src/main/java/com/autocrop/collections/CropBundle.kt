package com.autocrop.collections

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import com.autocrop.utilsandroid.approximateJpegSize
import kotlin.math.roundToInt

/**
 * Encapsulation of entirety of data being associated with crop
 */
data class CropBundle(val screenshot: ScreenshotParameters, var crop: Crop) {

    constructor(screenshotUri: Uri, screenshot: Bitmap, cropRect: Rect)
        : this(
            ScreenshotParameters(
                screenshotUri,
                screenshot.height,
                screenshot.approximateJpegSize()
            ),
            Crop(
                Bitmap.createBitmap(screenshot,0, cropRect.top, screenshot.width, cropRect.height()),
                cropRect
            )
        )

    fun discardedPercentage(): Int =
        (_discardedPercentage() * 100).roundToInt()

    fun discardedFileSize(): Int =
        (_discardedPercentage() * screenshot.approximateJpegSize).roundToInt()

    fun bottomOffset(): Int =
        screenshot.height - crop.rect.bottom

    private fun _discardedPercentage(): Float =
        (screenshot.height - crop.bitmap.height).toFloat() / screenshot.height.toFloat()
}

data class ScreenshotParameters(
    val uri: Uri,
    val height: Int,
    val approximateJpegSize: Int
)

data class Crop(
    val bitmap: Bitmap,
    val rect: Rect
)