package com.w2sv.flowfield.scene

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

/**
 * Manages the core Filament rendering infrastructure for 2D/3D scenes.
 *
 * This class is responsible for:
 * - Initializing and managing the Filament engine, renderer, and scene graph
 * - Setting up the orthographic camera for 2D rendering
 * - Configuring basic lighting for the scene
 * - Handling surface creation and swap chain management
 * - Executing the render loop and frame presentation
 * - Properly cleaning up all Filament resources
 *
 * Usage:
 * 1. Create instance with AssetManager for material loading
 * 2. Set rendering surface via setSurface()
 * 3. Scenes should use the exposed engine and scene to add content
 * 4. Call render() each frame to present to the surface
 * 5. Call destroy() when done to clean up resources
 */
internal class FilamentSceneManager {
    val engine: Engine
    val scene: Scene

    private val renderer: Renderer
    private val camera: Camera
    private val view: View
    private var swapChain: SwapChain? = null

    private var isDestroyed = false

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

    fun render() {
        check(!isDestroyed) { "FilamentSceneManager is destroyed" }
        swapChain?.let {
            val frameTimeNanos = System.nanoTime()
            if (renderer.beginFrame(it, frameTimeNanos)) {
                renderer.render(view)
                renderer.endFrame()
            }
        }
    }

    fun setSurface(surface: Surface) {
        check(!isDestroyed) { "FilamentSceneManager is destroyed" }
        swapChain = engine.createSwapChain(surface)
    }

    fun destroy() {
        if (isDestroyed) return

        engine.destroyRenderer(renderer)
        engine.destroyScene(scene)
        engine.destroyView(view)
        swapChain?.let { engine.destroySwapChain(it) }
        engine.destroy()

        isDestroyed = true
    }
}
