package com.w2sv.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.w2sv.kotlinutils.rounded
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

// TODO: write tests
@Parcelize
data class Crop(val bitmap: Bitmap, val edges: CropEdges, val discardedPercentage: Int, private val discardedKB: Long) : Parcelable {

    @IgnoredOnParcel
    val discardedFileSize: String by lazy {
        if (discardedKB >= 1000) {
            "${(discardedKB.toFloat() / 1000).rounded(1)}mb"
        } else {
            "${discardedKB}kb"
        }
    }
}
