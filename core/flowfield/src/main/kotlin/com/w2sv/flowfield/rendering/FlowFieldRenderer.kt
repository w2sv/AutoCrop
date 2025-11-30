package com.w2sv.flowfield.rendering

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.w2sv.flowfield.rendering.util.FpsLogger
import com.w2sv.flowfield.rendering.util.wrapper.FrameBuffer
import com.w2sv.flowfield.rendering.util.wrapper.Texture2D
import com.w2sv.flowfield.simulation.FlowField
import com.w2sv.flowfield.simulation.Particle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class FlowFieldRenderer(
    private var width: Int,
    private var height: Int,
    private val flowField: FlowField,
    private val particles: List<Particle>,
    logFps: Boolean
) : GLSurfaceView.Renderer {

    private val fpsLogger = if (logFps) FpsLogger() else null
    private lateinit var lineRenderer: LineRenderer
    private lateinit var quadRenderer: QuadRenderer

    private lateinit var fbo: FrameBuffer
    private lateinit var fboTexture: Texture2D

    @Volatile
    private var needsReset = false

    fun reset() {
        needsReset = true
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        clearColor()
        lineRenderer = LineRenderer(particles.size)
        quadRenderer = QuadRenderer()
        initializeFBO()
    }

    private fun initializeFBO() {
        fbo = FrameBuffer()
        fboTexture = Texture2D().apply { bind() }
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        fbo.whilstBound {
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, fboTexture.id, 0)
            if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                throw RuntimeException("FBO incomplete")
            }
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        }
    }

    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int
    ) {
        this.width = width
        this.height = height
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        fpsLogger?.onFrame()

        if (needsReset) {
            doReset()
        } else {
            renderFrame()
        }
    }

    private fun renderFrame() {
        flowField.prepareFrame()

        quadRenderer.fade(frameBuffer = fbo, texture = fboTexture)

        // Draw new trails
        lineRenderer.buildVertexBuffer {
            particles.forEach { p ->
                val angle = flowField.forceAngle(p.pos)
                p.update(angle, width, height)
                if (!p.shouldSkipDraw()) {
                    lineRenderer.addParticle(p)
                }
                p.afterDraw()
            }
        }
        // Fade existing trails with the threshold shader
        lineRenderer.draw(fbo, width, height)
        // Display the result
        quadRenderer.draw(fboTexture)
    }

    private fun doReset() {
        // Clear FBO to fully wipe trails
        fbo.whilstBound {
            clearColor()
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        }

        needsReset = false
    }

    private fun clearColor() {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
    }
}
