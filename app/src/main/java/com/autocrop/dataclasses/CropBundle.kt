package com.autocrop.dataclasses

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import com.autocrop.activities.cropping.fragments.cropping.cropped
import com.autocrop.utils.android.approximateJpegSize
import kotlin.math.roundToInt

/**
 * Encapsulation of data associated with crop
 */
data class CropBundle(val screenshot: Screenshot, val crop: Crop) {
    val discardedPercentage: Int
    val discardedFileSize: Int

    init {
        ((screenshot.height - crop.bitmap.height).toFloat() / screenshot.height.toFloat()).let { discardedPercentageF ->
            discardedPercentage = (discardedPercentageF * 100).roundToInt()
            discardedFileSize = (discardedPercentageF * screenshot.approximateJpegSize).roundToInt()
        }
    }

    companion object{
        fun assemble(screenshotUri: Uri, screenshot: Bitmap, cropRect: Rect): CropBundle =
            CropBundle(
                Screenshot(
                    screenshotUri,
                    screenshot.height,
                    screenshot.approximateJpegSize()
                ),
                Crop.fromScreenshot(
                    screenshot,
                    cropRect
                )
            )
    }

    val bottomOffset: Int by lazy {
        screenshot.height - crop.rect.bottom
    }

    fun identifier(): String = hashCode().toString()
}

data class Screenshot(
    val uri: Uri,
    val height: Int,
    val approximateJpegSize: Int
)

data class Crop(val bitmap: Bitmap, val rect: Rect) {

    companion object{
        fun fromScreenshot(screenshot: Bitmap, rect: Rect) = Crop(
            screenshot.cropped(rect),
            rect
        )
    }
}