package com.w2sv.flowfield.rendering

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.w2sv.flowfield.simulation.FlowField
import com.w2sv.flowfield.simulation.Particle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class FlowFieldRenderer(
    private val width: Int,
    private val height: Int,
    private val flowField: FlowField,
    private val particles: List<Particle>
) : GLSurfaceView.Renderer {

    // --- Line rendering ---
    private var lineProgram = 0
    private var vao = IntArray(1)
    private var vbo = IntArray(1)

    // Each particle produces 2 vertices (prev -> current) = 4 floats
    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(particles.size * 4 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    // --- FBO for persistent trails ---
    private var fbo = IntArray(1)
    private var fboTexture = IntArray(1)

    // --- Quad to draw FBO texture ---
    private var quadProgram = 0
    private var quadVao = IntArray(1)
    private var quadVbo = IntArray(1)
    private val quadVertices = floatArrayOf(
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f,
        -1f, 1f, 0f, 1f,
        1f, 1f, 1f, 1f
    )

    private val fpsLogger = FpsLogger()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)

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

        lineProgram = createProgram(vertexShaderCode, fragmentShaderCode)

        // VAO/VBO for lines
        GLES30.glGenVertexArrays(1, vao, 0)
        GLES30.glGenBuffers(1, vbo, 0)
        GLES30.glBindVertexArray(vao[0])
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, particles.size * 4 * 4, null, GLES30.GL_DYNAMIC_DRAW)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glBindVertexArray(0)

        // --- FBO ---
        GLES30.glGenFramebuffers(1, fbo, 0)
        GLES30.glGenTextures(1, fboTexture, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboTexture[0])
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo[0])
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, fboTexture[0], 0)
        if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("FBO incomplete")
        }
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        // --- Quad shader ---
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
        quadProgram = createProgram(quadVertexShaderCode, quadFragmentShaderCode)

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

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        fpsLogger.onFrame()

        // --- 1. FlowField advance ---
        flowField.prepareFrame()

        // --- 2. Update particles & build line buffer ---
        vertexBuffer.clear()
        particles.forEach { p ->
            val angle = flowField.forceAngle(p.pos)
            p.update(angle, width, height)
            if (!p.shouldSkipDraw()) {
                vertexBuffer.put(p.previousPos.x)
                vertexBuffer.put(p.previousPos.y)
                vertexBuffer.put(p.pos.x)
                vertexBuffer.put(p.pos.y)
            }
            p.afterDraw()
        }
        vertexBuffer.position(0)
        val numVertices = vertexBuffer.limit() / 2

        // --- 3. Draw lines into FBO ---
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo[0])
        GLES30.glUseProgram(lineProgram)

        // Pass resolution to vertex shader
        val resLoc = GLES30.glGetUniformLocation(lineProgram, "uResolution")
        GLES30.glUniform2f(resLoc, width.toFloat(), height.toFloat())

        GLES30.glBindVertexArray(vao[0])
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, vertexBuffer.limit() * 4, vertexBuffer)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, numVertices)
        GLES30.glBindVertexArray(0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        // --- 4. Draw FBO texture to screen ---
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(quadProgram)
        GLES30.glBindVertexArray(quadVao[0])
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboTexture[0])
        val texLoc = GLES30.glGetUniformLocation(quadProgram, "uTexture")
        GLES30.glUniform1i(texLoc, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)
    }
}
