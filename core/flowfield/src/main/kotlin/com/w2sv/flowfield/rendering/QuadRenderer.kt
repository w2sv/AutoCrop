package com.w2sv.flowfield.rendering

import android.opengl.GLES30
import com.w2sv.flowfield.rendering.util.GlProgram
import com.w2sv.flowfield.rendering.util.createProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class QuadRenderer {
    @GlProgram
    private val drawProgram: Int

    @GlProgram
    private val fadeProgram: Int

    private var vao = IntArray(1)
    private var vbo = IntArray(1)
    private val vertices = floatArrayOf(
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f,
        -1f, 1f, 0f, 1f,
        1f, 1f, 1f, 1f
    )

    init {
        val vertexShaderCode = """
            #version 300 es
            layout(location = 0) in vec2 aPos;
            layout(location = 1) in vec2 aTex;
            out vec2 vTex;
            void main() { vTex = aTex; gl_Position = vec4(aPos, 0.0, 1.0); }
        """.trimIndent()
        val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            in vec2 vTex;
            uniform sampler2D uTexture;
            out vec4 FragColor;
            void main() { FragColor = texture(uTexture, vTex); }
        """.trimIndent()
        val fadeFragShader = """
            #version 300 es
            precision mediump float;
            uniform float uAlpha;
            uniform sampler2D uTexture;
            in vec2 vTex;
            out vec4 FragColor;

            void main() {
                vec4 color = texture(uTexture, vTex);
                // Subtract from alpha only - preserves RGB intensity
                color.a = max(0.0, color.a - uAlpha);

                // Cheap cleanup: if alpha is very small, multiply to zero
                color.a *= step(0.005, color.a); // step() is branchless and fast

                FragColor = color;
            }
        """.trimIndent()
        drawProgram = createProgram(vertexShaderCode, fragmentShaderCode)
        fadeProgram = createProgram(vertexShaderCode, fadeFragShader)

        GLES30.glGenVertexArrays(1, vao, 0)
        GLES30.glGenBuffers(1, vbo, 0)
        GLES30.glBindVertexArray(vao[0])
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        val quadBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        quadBuffer.put(vertices).position(0)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * 4, quadBuffer, GLES30.GL_STATIC_DRAW)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 4 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 4 * 4, 2 * 4)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glBindVertexArray(0)
    }

    fun draw(textureId: Int) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(drawProgram)
        GLES30.glBindVertexArray(vao[0])
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        val texLoc = GLES30.glGetUniformLocation(drawProgram, "uTexture")
        GLES30.glUniform1i(texLoc, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)
    }

    fun fade(fbo: Int, textureId: Int) {
        GLES30.glUseProgram(fadeProgram)

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        GLES30.glBindVertexArray(vao[0])

        // Bind the FBO texture to operate on it
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)

        // Push shader inputs
        val alphaLoc = GLES30.glGetUniformLocation(fadeProgram, "uAlpha")
        val texLoc = GLES30.glGetUniformLocation(fadeProgram, "uTexture")
        GLES30.glUniform1i(texLoc, 0)
        GLES30.glUniform1f(alphaLoc, 0.005f)

        // Important: Replace the content, don't blend
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        // Unbind
        GLES30.glBindVertexArray(0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }
}
