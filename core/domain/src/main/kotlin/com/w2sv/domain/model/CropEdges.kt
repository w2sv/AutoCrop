package com.w2sv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropEdges(val top: Int, val bottom: Int) : Parcelable {

    val height: Int
        get() = bottom - top
}
