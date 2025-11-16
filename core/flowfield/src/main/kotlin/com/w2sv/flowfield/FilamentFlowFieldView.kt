package com.w2sv.flowfield

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.w2sv.flowfield.scene.FilamentSceneManager
import com.w2sv.flowfield.simulation.FlowField
import com.w2sv.flowfield.simulation.FlowFieldConfig
import com.w2sv.flowfield.simulation.Particle

class FilamentFlowFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr),
    SurfaceHolder.Callback {

    private lateinit var filamentManager: FilamentSceneManager
    private lateinit var particles: List<Particle>
    private lateinit var flowField: FlowField

    private var isRunning = false
    private val renderHandler = Handler(Looper.getMainLooper())
    private var lastFrameTime = 0L

    init {
        holder.addCallback(this)
        initializeSimulation()
    }

    private fun initializeSimulation() {
        particles = List(FlowFieldConfig.N_PARTICLES) {
            Particle.randomParticle(1000, 1000)
        }
        flowField = FlowField(
            FlowFieldConfig.FLOW_FIELD_GRANULARITY,
            FlowFieldConfig.FLOW_FIELD_Z_OFF_INCREMENT
        )
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Everything on UI thread - simplest approach
        filamentManager = FilamentSceneManager(context.assets)
        filamentManager.setSurface(holder.surface)
        filamentManager.setViewSize(width, height)
        filamentManager.initializeParticles(particles)
        startRendering()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        filamentManager.setViewSize(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopRendering()
    }

    private fun startRendering() {
        if (isRunning) return
        isRunning = true
        lastFrameTime = System.nanoTime()
        renderHandler.post(renderLoop)
    }

    private fun stopRendering() {
        isRunning = false
        renderHandler.removeCallbacks(renderLoop)
        filamentManager.destroy()
    }

    private val renderLoop = object : Runnable {
        override fun run() {
            if (!isRunning) return

            // Update simulation
            flowField.prepareFrame()
            particles.forEach { particle ->
                val angle = flowField.forceAngle(particle.pos)
                particle.update(angle, width, height)
            }
            particles.forEach { it.afterDraw() }

            // Update rendering
            filamentManager.update(0.016f) // Fixed timestep
            filamentManager.render()

            renderHandler.postDelayed(this, 16) // ~60 FPS
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRendering()
    }
}
