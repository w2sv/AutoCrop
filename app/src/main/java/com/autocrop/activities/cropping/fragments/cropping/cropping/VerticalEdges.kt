package com.autocrop.activities.cropping.fragments.cropping.cropping

data class VerticalEdges(val top: Int, val bottom: Int){
    constructor(values: Pair<Int, Int>):
        this(values.first, values.second)

    fun height(): Int =
        bottom - top

    fun toPair(): Pair<Int, Int> =
        top to bottom
}