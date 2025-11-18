package com.w2sv.flowfield.rendering

import android.opengl.GLES30
import com.w2sv.flowfield.rendering.util.GlProgram
import com.w2sv.flowfield.rendering.util.createProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class QuadRenderer {
    @GlProgram
    private val program: Int
    private var quadVao = IntArray(1)
    private var quadVbo = IntArray(1)
    private val quadVertices = floatArrayOf(
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f,
        -1f, 1f, 0f, 1f,
        1f, 1f, 1f, 1f
    )

    init {
        val quadVertexShaderCode = """
            #version 300 es
            layout(location = 0) in vec2 aPos;
            layout(location = 1) in vec2 aTex;
            out vec2 vTex;
            void main() { vTex = aTex; gl_Position = vec4(aPos, 0.0, 1.0); }
        """.trimIndent()
        val quadFragmentShaderCode = """
            #version 300 es
            precision mediump float;
            in vec2 vTex;
            uniform sampler2D uTexture;
            out vec4 FragColor;
            void main() { FragColor = texture(uTexture, vTex); }
        """.trimIndent()
        program = createProgram(quadVertexShaderCode, quadFragmentShaderCode)

        GLES30.glGenVertexArrays(1, quadVao, 0)
        GLES30.glGenBuffers(1, quadVbo, 0)
        GLES30.glBindVertexArray(quadVao[0])
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, quadVbo[0])
        val quadBuffer = ByteBuffer.allocateDirect(quadVertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        quadBuffer.put(quadVertices).position(0)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, quadVertices.size * 4, quadBuffer, GLES30.GL_STATIC_DRAW)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 4 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 4 * 4, 2 * 4)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glBindVertexArray(0)
    }

    fun draw(textureId: Int) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(program)
        GLES30.glBindVertexArray(quadVao[0])
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        val texLoc = GLES30.glGetUniformLocation(program, "uTexture")
        GLES30.glUniform1i(texLoc, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)
    }
}
