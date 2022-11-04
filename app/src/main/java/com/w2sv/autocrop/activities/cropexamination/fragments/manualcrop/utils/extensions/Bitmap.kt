package com.w2sv.autocrop.activities.cropexamination.fragments.manualcrop.utils.extensions

import android.graphics.Bitmap

fun Bitmap.maintainedPercentage(cropHeight: Float): Float =
    1 - (height.toFloat() - cropHeight) / height.toFloat()