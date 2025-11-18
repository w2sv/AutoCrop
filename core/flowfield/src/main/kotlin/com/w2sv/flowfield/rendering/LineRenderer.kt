package com.w2sv.flowfield.rendering

import android.opengl.GLES30
import com.w2sv.flowfield.rendering.util.GlProgram
import com.w2sv.flowfield.rendering.util.createProgram
import com.w2sv.flowfield.simulation.Particle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

internal class LineRenderer(particleCount: Int) {
    @GlProgram
    private val program: Int
    private var vao = IntArray(1)
    private var vbo = IntArray(1)

    // Each particle produces 2 vertices (prev -> current) = 4 floats
    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(particleCount * 4 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    init {
        // --- Line shader ---
        val vertexShaderCode = """
            #version 300 es
            layout(location = 0) in vec2 aPosPixel;

            uniform vec2 uResolution;   // (width, height)

            void main() {
                vec2 ndc = vec2(
                    (aPosPixel.x / uResolution.x) * 2.0 - 1.0,
                    1.0 - (aPosPixel.y / uResolution.y) * 2.0
                );

                gl_Position = vec4(ndc, 0.0, 1.0);
            }
        """.trimIndent()
        val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            out vec4 FragColor;
            void main() { FragColor = vec4(1.0,1.0,1.0,1.0); }
        """.trimIndent()

        program = createProgram(vertexShaderCode, fragmentShaderCode)

        // VAO/VBO for lines
        GLES30.glGenVertexArrays(1, vao, 0)
        GLES30.glGenBuffers(1, vbo, 0)
        GLES30.glBindVertexArray(vao[0])
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, particleCount * 4 * 4, null, GLES30.GL_DYNAMIC_DRAW)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glBindVertexArray(0)
    }

    fun buildVertexBuffer(block: () -> Unit) {
        vertexBuffer.clear()
        block()
        vertexBuffer.position(0)
    }

    fun addParticle(particle: Particle) {
        vertexBuffer.put(particle.previousPos.x)
        vertexBuffer.put(particle.previousPos.y)
        vertexBuffer.put(particle.pos.x)
        vertexBuffer.put(particle.pos.y)
    }

    fun draw(fbo: Int, width: Int, height: Int) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        GLES30.glUseProgram(program)

        val resLoc = GLES30.glGetUniformLocation(program, "uResolution")
        GLES30.glUniform2f(resLoc, width.toFloat(), height.toFloat())

        GLES30.glBindVertexArray(vao[0])
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, vertexBuffer.limit() * 4, vertexBuffer)

        // Use additive blending for alpha accumulation
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE)

        GLES30.glDrawArrays(GLES30.GL_LINES, 0, vertexBuffer.limit() / 2)
        GLES30.glBindVertexArray(0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }
}
