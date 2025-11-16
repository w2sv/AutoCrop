package com.w2sv.flowfield.simulation

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Particle with position, velocity and simple Euler integration.
 * previousPos is updated after drawing so the renderer can form a line segment.
 */
class Particle(
    val pos: Vec2,
    private val vel: Vec2,
    private val maxSpeed: Float
) {
    private val acc = Vec2(0f, 0f)
    val previousPos = Vec2(pos.x, pos.y)
    private var skipDraw = false

    /** Updates with a force specified as an angle (radians). Returns whether the position wrapped. */
    fun update(angle: Float, width: Int, height: Int): Boolean {
        // set acceleration to unit vector of angle
        acc.set(cos(angle), sin(angle))
        vel.add(acc).limit(maxSpeed)
        pos.add(vel)
        val wrapped = wrapPositionIfOutOfBounds(width, height)
        skipDraw = wrapped
        return wrapped
    }

    private fun wrapPositionIfOutOfBounds(width: Int, height: Int): Boolean {
        var wrapped = false
        if (pos.x >= width) { pos.x -= width; wrapped = true }
        else if (pos.x < 0f) { pos.x += width; wrapped = true }
        if (pos.y >= height) { pos.y -= height; wrapped = true }
        else if (pos.y < 0f) { pos.y += height; wrapped = true }
        return wrapped
    }

    /** Mark the previous() position to current pos after drawing */
    fun afterDraw() { previousPos.set(pos) }

    fun shouldSkipDraw(): Boolean {
        return skipDraw.also { if (it) skipDraw = false }
    }

    companion object {
        /** Factory that mirrors your Processing initialization */
        fun randomParticle(width: Int, height: Int): Particle {
            val vx = Random.nextInt(FlowFieldConfig.PARTICLE_START_VELOCITY_LOW,
                FlowFieldConfig.PARTICLE_START_VELOCITY_HIGH + 1).toFloat()
            val vy = Random.nextInt(FlowFieldConfig.PARTICLE_START_VELOCITY_LOW,
                FlowFieldConfig.PARTICLE_START_VELOCITY_HIGH + 1).toFloat()
            val max = Random.nextInt(FlowFieldConfig.PARTICLE_MAX_VELOCITY_LOW,
                FlowFieldConfig.PARTICLE_MAX_VELOCITY_HIGH + 1).toFloat()
            val px = Random.nextFloat() * width
            val py = Random.nextFloat() * height
            return Particle(Vec2(px, py), Vec2(vx, vy), max)
        }
    }
}
