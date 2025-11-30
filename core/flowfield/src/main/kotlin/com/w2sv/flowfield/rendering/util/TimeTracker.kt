package com.w2sv.flowfield.rendering.util

internal class TimeTracker {
    private val startTime = System.currentTimeMillis()
    val elapsedSeconds get() = (System.currentTimeMillis() - startTime) / 1000f
}
