package com.w2sv.autocrop.activities.examination.fragments.manualcrop.extensions

import android.graphics.Bitmap

fun Bitmap.maintainedPercentage(cropHeight: Float): Float =
    1 - (height.toFloat() - cropHeight) / height.toFloat()