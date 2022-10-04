package com.lyrebirdstudio.croppylib.fragment.cropview

import android.graphics.RectF

data class CropEdges(val top: Int, val bottom: Int) {
    constructor(edges: Pair<Int, Int>)
        : this(edges.first, edges.second)

    fun asRectF(cropWidth: Int): RectF =
        RectF(0F, top.toFloat(), cropWidth.toFloat(), bottom.toFloat())
}