package com.autocrop.utils

import android.graphics.Point
import kotlin.math.abs

fun manhattanNorm(a: Point, b: Point): Int = abs(a.x - b.x) + abs(a.y - b.y)