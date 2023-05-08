package com.w2sv.autocrop.activities.examination.comparison.model

import androidx.annotation.StringRes
import com.w2sv.autocrop.R

enum class ImageType(@StringRes val labelRes: Int) {
    Screenshot(R.string.original),
    Crop(R.string.cropped)
}