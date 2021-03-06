package com.autocrop.collections

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import com.autocrop.activities.cropping.fragments.cropping.cropped
import com.autocrop.utilsandroid.approximateJpegSize
import kotlin.math.roundToInt
import kotlin.properties.Delegates

/**
 * Encapsulation of entirety of data being associated with crop
 */
data class CropBundle(val screenshot: ScreenshotParameters, private var _crop: Crop) {

    var crop: Crop
        get() = _crop
        set(value) {
            _crop = value
            calculateCompositeParameters()
        }

    constructor(screenshotUri: Uri, screenshot: Bitmap, cropRect: Rect)
            : this(
        ScreenshotParameters(
            screenshotUri,
            screenshot.height,
            screenshot.approximateJpegSize()
        ),
        Crop.FromScreenshot(
            screenshot,
            cropRect
        )
    ){
        calculateCompositeParameters()
    }

    val discardedPercentage: Int get() = _discardedPercentage
    private var _discardedPercentage by Delegates.notNull<Int>()

    val discardedFileSize: Int get() = _discardedFileSize
    private var _discardedFileSize by Delegates.notNull<Int>()

    val bottomOffset: Int get() = _bottomOffset
    private var _bottomOffset by Delegates.notNull<Int>()

    private fun calculateCompositeParameters(){
        ((screenshot.height - crop.bitmap.height).toFloat() / screenshot.height.toFloat()).let { discardedPercentageF ->
            _discardedPercentage = (discardedPercentageF * 100).roundToInt()
            _discardedFileSize = (discardedPercentageF * screenshot.approximateJpegSize).roundToInt()
        }

        _bottomOffset = screenshot.height - crop.rect.bottom
    }
}

class ScreenshotParameters(
    val uri: Uri,
    val height: Int,
    val approximateJpegSize: Int
)

sealed class Crop(
    val bitmap: Bitmap,
    val rect: Rect
){
    class FromScreenshot(screenshot: Bitmap, rect: Rect)
        : Crop(
            screenshot.cropped(rect),
            rect
        )
}