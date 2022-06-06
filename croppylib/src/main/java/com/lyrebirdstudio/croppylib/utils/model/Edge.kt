package com.lyrebirdstudio.croppylib.utils.model

import com.lyrebirdstudio.croppylib.utils.model.Edge.*

enum class Edge {
    NONE,
    LEFT,
    TOP,
    RIGHT,
    BOTTOM
}

fun Edge.opposite() {
    when (this) {
        LEFT -> RIGHT
        TOP -> BOTTOM
        RIGHT -> LEFT
        BOTTOM -> TOP
        NONE -> NONE
    }
}