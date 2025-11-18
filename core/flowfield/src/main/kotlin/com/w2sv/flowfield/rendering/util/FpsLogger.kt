package com.w2sv.flowfield.rendering.util

import slimber.log.d

class FpsLogger {

    private var frameCount = 0
    private var lastTime = System.currentTimeMillis()
    private var fps = 0f

    /**
     * Call this once per frame. Returns the current FPS if updated (once per second), else null.
     */
    fun onFrame() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime >= 1000L) {
            fps = frameCount * 1000f / (currentTime - lastTime)
            d { "FPS: $fps" }
            frameCount = 0
            lastTime = currentTime
            fps
        }
    }
}
