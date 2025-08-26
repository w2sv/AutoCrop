package com.w2sv.autocrop.ui.screen.pager.model

import androidx.annotation.StringRes
import com.w2sv.core.common.R.string as Strings

enum class CropProcedure(@StringRes val notificationMessageRes: Int?) {
    Discard(null),
    Save(Strings.saved_crop)
}
