package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.core.common.R.string as Strings

enum class CropAdjustmentMode(@StringRes val labelRes: Int) {
    Manual(Strings.manual),
    EdgeSelection(Strings.edge_selection)
}
