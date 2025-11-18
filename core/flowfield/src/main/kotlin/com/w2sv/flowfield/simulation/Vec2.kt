package com.w2sv.flowfield.simulation

import kotlin.math.sqrt

/** Minimal 2D vector used by the simulation (mutable for performance). */
internal data class Vec2(var x: Float = 0.0f, var y: Float = 0.0f) {
    fun setTo(other: Vec2) {
        x = other.x
        y = other.y
    }

    fun setTo(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun plus(other: Vec2): Vec2 {
        x += other.x
        y += other.y
        return this
    }

    fun multiplyBy(scalar: Float): Vec2 {
        x *= scalar
        y *= scalar
        return this
    }

    fun length(): Float =
        sqrt(x * x + y * y)

    fun limit(max: Float): Vec2 {
        val len = length()
        if (len > max) {
            multiplyBy(max / len)
        }
        return this
    }
}
