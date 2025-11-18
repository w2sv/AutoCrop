package com.w2sv.flowfield.rendering

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.w2sv.flowfield.simulation.FlowField
import com.w2sv.flowfield.simulation.Particle
import com.w2sv.flowfield.simulation.Vec2

class FlowFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var initialized = false
    private lateinit var renderer: FlowFieldRenderer

    init {
        setEGLContextClientVersion(3)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (!initialized && w > 0 && h > 0) {
            initialized = true

            // Initialize particles
            val particleCount = 600
            val particles = List(particleCount) {
                val pos = Vec2((0 until w).random().toFloat(), (0 until h).random().toFloat())
                Particle(pos, Vec2(0f, 0f), maxSpeed = 2f)
            }

            // Initialize flow field
            val flowField = FlowField(200, 0.01f, 0.1f, 4)

            // Initialize renderer with actual view size
            renderer = FlowFieldRenderer(w, h, flowField, particles)
            setRenderer(renderer)
            renderMode = RENDERMODE_CONTINUOUSLY
        }
    }
}
