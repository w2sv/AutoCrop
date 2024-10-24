package com.w2sv.cropbundle.cropping.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropEdges(val top: Int, val bottom: Int) : Parcelable {

    constructor(edges: Pair<Int, Int>) : this(edges.first, edges.second)

    constructor(edges: List<Int>) : this(edges.first(), edges.last())

    val height: Int
        get() = bottom - top

    companion object {
        const val EXTRA = "com.w2sv.autocrop.extra.CROP_EDGES"
    }
}