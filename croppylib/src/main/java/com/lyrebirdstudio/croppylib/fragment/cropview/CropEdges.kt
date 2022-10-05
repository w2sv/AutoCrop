package com.lyrebirdstudio.croppylib.fragment.cropview

import android.graphics.RectF

data class CropEdges<T: Number>(val top: T, val bottom: T) {
    constructor(edges: Pair<T, T>)
        : this(edges.first, edges.second)

    fun asRectF(cropWidth: Int): RectF =
        RectF(0F, top.toFloat(), cropWidth.toFloat(), bottom.toFloat())

    val height: Int get() = bottom.toInt() - top.toInt()
    val heightF: Float get() = bottom.toFloat() - top.toFloat()

    fun flattenToString(): String = "$top $bottom"

    fun cropsEdgesI(): CropEdges<Int> =
        CropEdges(top.toInt(), bottom.toInt())
}
