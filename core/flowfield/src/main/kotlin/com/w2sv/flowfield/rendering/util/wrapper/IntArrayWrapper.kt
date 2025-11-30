package com.w2sv.flowfield.rendering.util.wrapper

abstract class IntArrayWrapper {
    protected val buffer = IntArray(1)

    val id: Int get() = buffer[0]
}
