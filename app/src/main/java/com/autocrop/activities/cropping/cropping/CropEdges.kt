package com.autocrop.activities.cropping.cropping

data class CropEdges(val top: Int, val bottom: Int){
    constructor(values: Pair<Int, Int>):
        this(values.first, values.second)

    val height: Int get() = bottom - top

    fun toPair(): Pair<Int, Int> =
        top to bottom
}