package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.domain.R

enum class CropAdjustmentMode(@StringRes val labelRes: Int) {
    Manual(R.string.manual),
    EdgeSelection(R.string.edge_selection)
}