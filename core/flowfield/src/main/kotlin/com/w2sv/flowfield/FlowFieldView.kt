package com.w2sv.flowfield

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.w2sv.flowfield.rendering.FlowFieldRenderer
import com.w2sv.flowfield.simulation.FlowField
import com.w2sv.flowfield.simulation.Particle
import com.w2sv.flowfield.simulation.Vec2
import kotlin.random.Random

class FlowFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var initialized = false

    init {
        setEGLContextClientVersion(3)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (!initialized && w > 0 && h > 0) {
            initialized = true

            // Initialize particles
            val particles = List(Config.PARTICLE_COUNT) {
                Particle(
                    pos = Vec2(Random.nextFloat() * w, Random.nextFloat() * h),
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
            setRenderer(FlowFieldRenderer(w, h, flowField, particles))
            renderMode = RENDERMODE_CONTINUOUSLY
        }
    }
}
