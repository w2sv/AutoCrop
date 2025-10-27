package com.w2sv.autocrop.ui.screen.cropadjustment.extensions

import android.graphics.Bitmap
import android.graphics.RectF

fun Bitmap.rectF(): RectF =
    RectF(
        0f,
        0f,
        width.toFloat(),
        height.toFloat()
    )
