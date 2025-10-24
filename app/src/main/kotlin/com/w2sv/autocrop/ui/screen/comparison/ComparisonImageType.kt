package com.w2sv.autocrop.ui.screen.comparison

import androidx.annotation.StringRes
import com.w2sv.core.common.R

enum class ComparisonImageType(@StringRes val labelRes: Int) {
    Original(R.string.original),
    Crop(R.string.crop)
}
