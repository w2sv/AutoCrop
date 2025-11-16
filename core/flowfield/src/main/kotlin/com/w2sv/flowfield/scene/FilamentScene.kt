package com.w2sv.flowfield.scene

import android.content.res.AssetManager
import com.google.android.filament.Engine
import com.google.android.filament.Scene

interface FilamentScene {
    fun initialize(engine: Engine, scene: Scene, assetManager: AssetManager, viewWidth: Int, viewHeight: Int)
    fun onViewResized(width: Int, height: Int)
    fun update(deltaTime: Float)
    fun destroy()
}
