package com.w2sv.flowfield.rendering.util.wrapper

import android.opengl.GLES30

internal class ArrayBuffer : IntArrayWrapper() {

    init {
        GLES30.glGenBuffers(1, buffer, 0)
    }

    fun bind() {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, id)
    }
}
