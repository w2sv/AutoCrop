package com.w2sv.flowfield

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.w2sv.flowfield.rendering.FlowFieldRenderer
import com.w2sv.flowfield.simulation.FlowField
import com.w2sv.flowfield.simulation.Particle
import com.w2sv.flowfield.simulation.Vec2
import slimber.log.i
import kotlin.random.Random

class FlowFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var isInitialized = false

    init {
        i { "Init $this | Id=${this.id}" }
        setEGLContextClientVersion(3)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        i { "onAttachToWindow" }
        setEGLContextClientVersion(3)
        preserveEGLContextOnPause = true
    }

    override fun onPause() {
        i { "onPause" }
        super.onPause()
    }

    override fun onResume() {
        i { "onResume" }
        if (isInitialized) {
            super.onResume()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        i { "onSizeChanged $w x $h" }

        if (!isInitialized && w > 0 && h > 0) {
            initializeRendering(w, h)
            isInitialized = true
        }
    }

    private fun initializeRendering(width: Int, height: Int) {
        // Initialize particles
        val particles = List(Config.PARTICLE_COUNT) {
            Particle(
                pos = Vec2(Random.nextFloat() * width, Random.nextFloat() * height),
                maxVelocity = Config.PARTICLE_MAX_VELOCITY
            )
        }

        // Initialize flow field
        val flowField = FlowField(
            cellsPerPixel = Config.CELLS_PER_PIXEL,
            timeIncrement = Config.NOISE_SAMPLING_TEMPORAL_INCREMENT,
            noiseScale = 0.1f,
            radianSamplingCoeff = Config.RADIAN_SAMPLING_COEFFICIENT
        )

        // Initialize renderer with actual view size
        val renderer = FlowFieldRenderer(
            width = width,
            height = height,
            flowField = flowField,
            particles = particles,
            logFps = true
        )
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
