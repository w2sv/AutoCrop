package com.autocrop.collections

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import com.autocrop.utilsandroid.approximateJpegSize
import kotlin.math.roundToInt
import kotlin.properties.Delegates

/**
 * Encapsulation of entirety of data being associated with crop
 */
data class CropBundle(val screenshot: ScreenshotParameters, var crop: Crop) {

    init {
        calculateCompositeParameters()
    }

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

    val discardedPercentage: Int get() = _discardedPercentage

    private var _discardedPercentage: Int by Delegates.notNull()
    val discardedFileSize: Int get() = _discardedFileSize

    private var _discardedFileSize: Int by Delegates.notNull()
    val bottomOffset: Int get() = _bottomOffset

    private var _bottomOffset: Int by Delegates.notNull()

    private fun calculateCompositeParameters(){
        ((screenshot.height - crop.bitmap.height).toFloat() / screenshot.height.toFloat()).let { discardedPercentageF ->
            _discardedPercentage = (discardedPercentageF * 100).roundToInt()
            _discardedFileSize = (discardedPercentageF * screenshot.approximateJpegSize).roundToInt()
        }

        _bottomOffset = screenshot.height - crop.rect.bottom
    }

    fun setAdjustedCrop(adjustedCrop: Crop){
        crop = adjustedCrop
        calculateCompositeParameters()
    }
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