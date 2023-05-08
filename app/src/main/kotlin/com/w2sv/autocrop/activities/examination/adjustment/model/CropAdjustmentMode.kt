package com.w2sv.autocrop.activities.examination.adjustment.model

import androidx.annotation.StringRes
import com.w2sv.autocrop.R

enum class CropAdjustmentMode(@StringRes val labelRes: Int) {
    Manual(R.string.manual),
    EdgeSelection(R.string.edge_selection)
}