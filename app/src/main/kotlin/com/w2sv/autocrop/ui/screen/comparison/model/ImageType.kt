package com.w2sv.autocrop.ui.screen.comparison.model

import androidx.annotation.StringRes
import com.w2sv.core.common.R.string as Strings

enum class ImageType(@StringRes val labelRes: Int) {
    Screenshot(Strings.original),
    Crop(Strings.cropped)
}
