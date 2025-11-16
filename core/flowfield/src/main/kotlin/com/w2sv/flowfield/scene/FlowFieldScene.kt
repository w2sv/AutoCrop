package com.w2sv.flowfield.scene

import android.content.res.AssetManager
import com.google.android.filament.Engine
import com.google.android.filament.Scene
import com.w2sv.flowfield.simulation.FlowField
import com.w2sv.flowfield.simulation.FlowFieldConfig
import com.w2sv.flowfield.simulation.Particle
import kotlin.properties.Delegates

class FlowFieldScene : FilamentScene {
    private lateinit var trailRenderer: ParticleTrailRenderer
    private lateinit var particles: List<Particle>
    private lateinit var flowField: FlowField
    private var viewWidth by Delegates.notNull<Int>()
    private var viewHeight by Delegates.notNull<Int>()

    override fun initialize(engine: Engine, scene: Scene, assetManager: AssetManager, viewWidth: Int, viewHeight: Int) {
        this.viewWidth = viewWidth
        this.viewHeight = viewHeight

        trailRenderer = ParticleTrailRenderer(engine, scene, assetManager)
        initializeSimulation()
        trailRenderer.initializeParticles(particles)
    }

    override fun onViewResized(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        trailRenderer.setViewSize(width, height)
    }

    override fun update(deltaTime: Float) {
        flowField.prepareFrame()
        particles.forEach { particle ->
            val angle = flowField.forceAngle(particle.pos)
            particle.update(angle, viewWidth, viewHeight)
        }
        particles.forEach { it.afterDraw() }
        trailRenderer.update(deltaTime)
    }

    override fun destroy() {
        trailRenderer.destroy()
    }

    private fun initializeSimulation() {
        particles = List(FlowFieldConfig.N_PARTICLES) {
            Particle.randomParticle(viewWidth, viewHeight)
        }
        flowField = FlowField(
            FlowFieldConfig.FLOW_FIELD_GRANULARITY,
            FlowFieldConfig.FLOW_FIELD_Z_OFF_INCREMENT
        )
    }
}
