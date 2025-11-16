package com.w2sv.flowfield.scene

import android.content.res.AssetManager
import android.view.Surface
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Filament
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.w2sv.flowfield.simulation.Particle

class FilamentSceneManager(assetManager: AssetManager) {
    private val engine: Engine
    private val renderer: Renderer
    private val scene: Scene
    private val camera: Camera
    private val view: View
    private var swapChain: SwapChain? = null

    private val trailRenderer: ParticleTrailRenderer

    init {
        // Initialize Filament
        Filament.init()

        engine = Engine.create()
        renderer = engine.createRenderer()
        scene = engine.createScene()

        val cameraEntity = EntityManager.get().create()
        camera = engine.createCamera(cameraEntity)
        view = engine.createView().apply {
            setScene(scene)
            setCamera(camera)
        }

        setup2DCamera()
        setupLighting()

        trailRenderer = ParticleTrailRenderer(engine, scene, assetManager)
    }

    private fun setup2DCamera() {
        // Set up orthographic projection for 2D
        val left = -1.0
        val right = 1.0
        val bottom = -1.0
        val top = 1.0
        val near = 0.0
        val far = 10.0

        camera.setProjection(
            Camera.Projection.ORTHO,
            left, right, bottom, top, near, far
        )

        // Position camera for 2D view
        camera.lookAt(
            0.0, 0.0, 5.0,  // eye
            0.0, 0.0, 0.0,  // center
            0.0, 1.0, 0.0   // up
        )
    }

    private fun setupLighting() {
        // Add simple ambient light
        val lightEntity = EntityManager.get().create()

        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(1.0f)
            .direction(0.0f, 0.0f, -1.0f)
            .build(engine, lightEntity)

        scene.addEntity(lightEntity)
    }

    fun initializeParticles(particles: List<Particle>) {
        trailRenderer.initializeParticles(particles)
    }

    fun update(deltaTime: Float) {
        trailRenderer.update(deltaTime)
    }

    fun render() {
        swapChain?.let {
            val frameTimeNanos = System.nanoTime()
            if (renderer.beginFrame(it, frameTimeNanos)) {
                renderer.render(view)
                renderer.endFrame()
            }
        }
    }

    fun setSurface(surface: Surface) {
        swapChain = engine.createSwapChain(surface)
    }

    fun setViewSize(width: Int, height: Int) {
        trailRenderer.setViewSize(width, height)
    }

    fun destroy() {
        trailRenderer.destroy()

        engine.destroyRenderer(renderer)
        engine.destroyScene(scene)
        engine.destroyCameraComponent(camera.entity)
        engine.destroyView(view)
        swapChain?.let { engine.destroySwapChain(it) }
        engine.destroy()
    }
}
