package com.w2sv.cropbundle.cropping

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvException
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import slimber.log.d
import kotlin.system.measureTimeMillis

// Note: Range.s included, Range.e excluded
internal fun Mat.cropArea(edges: CropEdges): Mat =
    rowRange(edges.top, edges.bottom)

internal fun Mat.multiChannelMean(): Double =
    Core.mean(this).`val`.average()

internal fun Mat.singleChannelMean(): Double =
    Core.mean(this).`val`.first()

internal fun Bitmap.getMat(): Mat {
    val mat = Mat()
    Utils.bitmapToMat(this, mat)
    return mat
}

internal fun Mat.logInfo(matrixName: String) {
    d { "Cropping | ${matrixName}.nRows=${rows()} | nCols=${cols()}" }

    d { "Cropping | ${matrixName}.element=${get(0, 0).toList()}; ${channels()} channels" }

    val mean = MatOfDouble()
    val stdDev = MatOfDouble()
    Core.meanStdDev(this, mean, stdDev)

    d { "Cropping | ${matrixName}.multiChannelMean=${mean.toList()}" }
    d { "Cropping | ${matrixName}.std=${stdDev.toList()}" }

    try {
        val minMaxLoc = Core.minMaxLoc(this)
        d { "Cropping | ${matrixName}.min=${minMaxLoc.minVal}" }
        d { "Cropping | ${matrixName}.max=${minMaxLoc.maxVal}" }
    }
    catch (e: CvException) {
        val minMax = getMinMaxColorValues()
        d { "Cropping | ${matrixName}.min=${minMax.first}" }
        d { "Cropping | ${matrixName}.max=${minMax.second}" }
    }
}

private fun Mat.getMinMaxColorValues(): Pair<Double, Double> {
    var min = Double.POSITIVE_INFINITY
    var max = Double.NEGATIVE_INFINITY

    (0 until rows()).forEach { i ->
        (0 until cols()).forEach { j ->
            get(i, j).forEach { color ->
                if (color < min)
                    min = color
                if (color > max)
                    max = color
            }
        }
    }

    return min to max
}

internal inline fun <T> measured(methodLabel: String, f: () -> T): T {
    var result: T
    measureTimeMillis {
        result = f()
    }
        .also {
            d {
                "$methodLabel completed in ${it}ms"
            }
        }

    return result
}