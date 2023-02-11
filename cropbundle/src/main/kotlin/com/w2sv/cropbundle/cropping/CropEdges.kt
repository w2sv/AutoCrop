package com.w2sv.cropbundle.cropping

import android.graphics.RectF
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropEdges(val top: Int, val bottom: Int) : Parcelable {

    constructor(edges: Pair<Int, Int>)
            : this(edges.first, edges.second)

    constructor(edges: List<Int>)
            : this(edges.first(), edges.last())

    fun asRectF(cropWidth: Int): RectF =
        RectF(0F, top.toFloat(), cropWidth.toFloat(), bottom.toFloat())

    val height: Int get() = bottom - top
}