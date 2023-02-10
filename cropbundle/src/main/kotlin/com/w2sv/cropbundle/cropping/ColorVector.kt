package com.w2sv.cropbundle.cropping

import kotlin.math.abs

class ColorVector(alpha: Float, red: Float, green: Float, blue: Float) : List<Float> by listOf(
    red,
    green,
    blue,
    alpha
) {

    constructor(argb: Int) : this(
        (argb shr 24 and 0xff).toFloat(),
        (argb shr 16 and 0xff).toFloat(),
        (argb shr 8 and 0xff).toFloat(),
        (argb and 0xff).toFloat()
    )

    val red: Float
        get() = get(0)
    val green: Float
        get() = get(1)
    val blue: Float
        get() = get(2)
    val alpha: Float
        get() = get(3)

    operator fun plus(other: ColorVector): ColorVector =
        ColorVector(
            alpha + other.alpha,
            red + other.red,
            green + other.green,
            blue + other.blue
        )

    operator fun div(scalar: Float): ColorVector =
        ColorVector(
            alpha / scalar,
            red / scalar,
            blue / scalar,
            green / scalar
        )
}

fun absMeanDifference(a: ColorVector, b: ColorVector): Float =
    (
            abs(a.alpha - b.alpha) +
                    abs(a.red - b.red) +
                    abs(a.green - b.green) +
                    abs(a.blue - b.blue)
            ) / 4f

fun Iterator<ColorVector>.mean(): ColorVector {
    var mean = next()
    for (element in this) {
        mean = (mean + element) / 2F
    }
    return mean
}