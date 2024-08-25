package com.w2sv.autocrop.ui.screen.pager.model

import androidx.annotation.StringRes
import com.w2sv.autocrop.R

enum class CropProcedure(@StringRes val notificationMessageRes: Int?) {
    Discard(null),
    Save(R.string.saved_crop)
}