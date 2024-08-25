package com.w2sv.autocrop.ui.screen.comparison.model

import androidx.annotation.StringRes
import com.w2sv.autocrop.R

enum class ImageType(@StringRes val labelRes: Int) {
    Screenshot(R.string.original),
    Crop(R.string.cropped)
}