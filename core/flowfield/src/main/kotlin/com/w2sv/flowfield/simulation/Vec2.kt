package com.w2sv.flowfield.simulation

/** Minimal 2D vector used by the simulation (mutable for performance). */
class Vec2(var x: Float = 0f, var y: Float = 0f) {
    fun set(other: Vec2) { x = other.x; y = other.y }
    fun set(x: Float, y: Float) { this.x = x; this.y = y }
    fun copy() = Vec2(x, y)

    fun add(other: Vec2): Vec2 { x += other.x; y += other.y; return this }
    fun addXY(dx: Float, dy: Float): Vec2 { x += dx; y += dy; return this }

    fun mul(s: Float): Vec2 { x *= s; y *= s; return this }
    fun length(): Float = kotlin.math.sqrt(x * x + y * y)
    fun limit(max: Float): Vec2 {
        val len = length()
        if (len > max && len > 0f) {
            val f = max / len
            x *= f; y *= f
        }
        return this
    }
}
