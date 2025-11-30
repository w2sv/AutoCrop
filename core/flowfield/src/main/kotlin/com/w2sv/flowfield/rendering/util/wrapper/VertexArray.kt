package com.w2sv.flowfield.rendering.util.wrapper

import android.opengl.GLES30

internal class VertexArray : IntArrayWrapper() {

    init {
        GLES30.glGenVertexArrays(1, buffer, 0)
    }

    fun whilstBound(block: () -> Unit) {
        GLES30.glBindVertexArray(id)
        block()
        GLES30.glBindVertexArray(0)
    }
}
