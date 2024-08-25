package com.w2sv.autocrop.ui.screen.cropadjustment.extensions

import android.graphics.Bitmap
import android.graphics.RectF

fun Bitmap.maintainedPercentage(cropHeight: Float): Float =
    1 - (height.toFloat() - cropHeight) / height.toFloat()

fun Bitmap.getRectF(): RectF =
    RectF(
        0f,
        0f,
        width.toFloat(),
        height.toFloat(),
    )