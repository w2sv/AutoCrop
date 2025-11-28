package com.w2sv.flowfield.simulation

import kotlin.math.cos
import kotlin.math.sin

/**
 * Particle with position, velocity and simple Euler integration.
 * previousPos is updated after drawing so the renderer can form a line segment.
 */
internal class Particle(val pos: Vec2, private val maxVelocity: Float) {
    private val vel = Vec2()
    private val acc = Vec2()
    val previousPos = Vec2(pos.x, pos.y)
    private var skipDraw = false

    /** Updates with a force specified as an angle (radians). Returns whether the position wrapped. */
    fun update(
        angle: Float,
        width: Int,
        height: Int
    ) {
        // set acceleration to unit vector of angle
        acc.setTo(cos(angle), sin(angle))
        vel.plus(acc).limit(maxVelocity)
        pos.plus(vel)
        wrapPositionIfOutOfBounds(width, height)
    }

    private fun wrapPositionIfOutOfBounds(width: Int, height: Int) {
        if (pos.x >= width) {
            pos.x -= width
            skipDraw = true
        } else if (pos.x < 0f) {
            pos.x += width
            skipDraw = true
        }
        if (pos.y >= height) {
            pos.y -= height
            skipDraw = true
        } else if (pos.y < 0f) {
            pos.y += height
            skipDraw = true
        }
    }

    /** Mark the previous() position to current pos after drawing */
    fun afterDraw() {
        previousPos.setTo(pos)
    }

    fun shouldSkipDraw(): Boolean =
        skipDraw.also { if (it) skipDraw = false }
}
