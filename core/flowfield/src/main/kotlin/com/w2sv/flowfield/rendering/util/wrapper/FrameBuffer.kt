package com.w2sv.flowfield.rendering.util.wrapper

import android.opengl.GLES30

internal class FrameBuffer: IntArrayWrapper() {

    init {
        GLES30.glGenFramebuffers(1, buffer, 0)
    }

    fun whilstBound(block: () -> Unit) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, id)
        block()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }
}
