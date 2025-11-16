package com.w2sv.flowfield

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.w2sv.flowfield.scene.FilamentScene
import com.w2sv.flowfield.scene.FilamentSceneManager

class FilamentRenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr),
    SurfaceHolder.Callback {

    private lateinit var filamentManager: FilamentSceneManager
    private lateinit var scene: FilamentScene
    private var isFilamentInitialized = false

    private var isRunning = false
    private val renderHandler = Handler(Looper.getMainLooper())

    init {
        holder.addCallback(this)
    }

    fun setScene(scene: FilamentScene) {
        this.scene = scene
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        filamentManager = FilamentSceneManager()
        filamentManager.setSurface(holder.surface)

        isFilamentInitialized = true
        scene.initialize(filamentManager.engine, filamentManager.scene, context.assets, width, height)
        startRendering()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (isFilamentInitialized) {
            scene.onViewResized(width, height)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopRendering()
        isFilamentInitialized = false
    }

    private fun startRendering() {
        if (isRunning) return
        isRunning = true
        renderHandler.post(renderLoop)
    }

    private fun stopRendering() {
        isRunning = false
        renderHandler.removeCallbacks(renderLoop)
        scene.destroy()
        filamentManager.destroy()
    }

    private val renderLoop = object : Runnable {
        override fun run() {
            if (!isRunning) return

            scene.update(0.016f)
            filamentManager.render()

            renderHandler.postDelayed(this, 16)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRendering()
    }
}
