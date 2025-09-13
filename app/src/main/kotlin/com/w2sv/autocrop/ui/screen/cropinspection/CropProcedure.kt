package com.w2sv.autocrop.ui.screen.cropinspection

import androidx.annotation.StringRes
import com.w2sv.core.common.R

enum class CropProcedure(@StringRes val notificationMessageRes: Int?) {
    Discard(null),
    Save(R.string.saved_crop)
}
