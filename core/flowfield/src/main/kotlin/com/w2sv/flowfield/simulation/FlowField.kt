package com.w2sv.flowfield.simulation

import kotlin.math.PI
import kotlin.math.floor

/**
 * Generates a direction (angle in radians) for a given particle position.
 * Caches a precomputed noise-x offset per grid cell (sparse cache).
 */
internal class FlowField(
    private val cellsPerPixel: Float,
    private val timeIncrement: Float,
    private val noiseScale: Float = 0.1f,
    radianSamplingCoeff: Float
) {
    private val noiseToRadiansScale = 2f * PI.toFloat() * radianSamplingCoeff
    private var time = 0f

    private val xNoiseCache = HashMap<Long, Float>()

    /** advance temporal z slice (call once per frame) */
    fun prepareFrame() {
        time += timeIncrement
    }

    /** Returns an angle in radians for the given world position. */
    fun forceAngle(pos: Vec2): Float {
        // compute grid cell indices corresponding to pos
        val xCell = floor(pos.x * cellsPerPixel).toInt() + 1
        val yCell = floor(pos.y * cellsPerPixel).toInt() + 1
        val key = packedLong(xCell, yCell)

        val xNoise = xNoiseCache.getOrPut(key) { noiseXOffset(xCell, yCell, noiseScale) }
        return ProcessingNoise.noise(xNoise, 0f, time) * noiseToRadiansScale
    }

    private fun packedLong(a: Int, b: Int): Long =
        (a.toLong() shl 32) or (b.toLong() and 0xFFFFFFFFL)

    /**
     * Cantor-inspired pairing function scaled for noise input.
     * Ensures (x,y) map uniquely to a float and keeps values small for smooth noise.
     */
    private fun noiseXOffset(
        x: Int,
        y: Int,
        scale: Float
    ): Float {
        val s = (x + y).toFloat()
        val paired = (s * (s + 1f) * 0.5f) + y
        return paired * scale
    }
}
