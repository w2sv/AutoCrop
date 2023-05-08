package com.w2sv.autocrop.activities.examination.pager.model

import androidx.annotation.StringRes
import com.w2sv.autocrop.R

enum class CropProcedure(@StringRes val notificationMessageRes: Int) {
    Discard(R.string.discarded_crop),
    Save(R.string.saved_crop)
}