package com.w2sv.flowfield.rendering

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.w2sv.flowfield.rendering.util.FpsLogger
import com.w2sv.flowfield.simulation.FlowField
import com.w2sv.flowfield.simulation.Particle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class FlowFieldRenderer(
    private var width: Int,
    private var height: Int,
    private val flowField: FlowField,
    private val particles: List<Particle>,
    logFps: Boolean,
) : GLSurfaceView.Renderer {

    private val fpsLogger = if (logFps) FpsLogger() else null
    private lateinit var lineRenderer: LineRenderer
    private lateinit var quadRenderer: QuadRenderer

    private var fbo = IntArray(1)
    private var fboTexture = IntArray(1)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        lineRenderer = LineRenderer(particles.size)
        quadRenderer = QuadRenderer()
        initializeFBO()
    }

    private fun initializeFBO() {
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
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        fpsLogger?.onFrame()

        flowField.prepareFrame()

        quadRenderer.fade(fbo = fbo[0], textureId = fboTexture[0])

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
        lineRenderer.draw(fbo[0], width, height)
        // Display the result
        quadRenderer.draw(fboTexture[0])
    }
}
