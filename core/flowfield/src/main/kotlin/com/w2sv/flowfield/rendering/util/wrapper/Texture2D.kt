package com.w2sv.flowfield.rendering.util.wrapper

import android.opengl.GLES30

internal class Texture2D: IntArrayWrapper() {

    init {
        GLES30.glGenTextures(1, buffer, 0)
    }

    fun bind() {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id)
    }

    fun bindToUnit0() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id)
    }
}
