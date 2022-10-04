package com.autocrop.activities.cropping.fragments.cropping.cropping

import android.graphics.Bitmap

private const val INTRA_ROW_FLUCTUATION_THRESHOLD = 5F
private const val MONOCHROME_THRESHOLD = 20F
private const val COMPARISONS_PER_ROW = 10

fun Bitmap.cropEdgesCandidates(): List<VerticalEdges>{
    val sampleStep = width / COMPARISONS_PER_ROW
    val candidates = mutableListOf<VerticalEdges>()

    var (yStartTopEdgeQuery, nonCropAreaMonochrome) = getBottomEdge(0, sampleStep, null).run {
        first + 1 to second
    }

    if (nonCropAreaMonochrome != null){
        while (yStartTopEdgeQuery != height){
            val topEdge = getTopEdge(yStartTopEdgeQuery, sampleStep, nonCropAreaMonochrome!!)
            if (topEdge != null){
                val bottomEdge = getBottomEdge(topEdge.first + 1, sampleStep, topEdge.second)
                candidates.add(VerticalEdges(topEdge.first, bottomEdge.first))

                yStartTopEdgeQuery = bottomEdge.first + 1
                nonCropAreaMonochrome = bottomEdge.second
            }
            else
                break
        }
    }

    return candidates.toList()
}

private fun Bitmap.getTopEdge(yStart: Int,
                              sampleStep: Int,
                              nonCropAreaMonochrome: ColorVector): Pair<Int, ColorVector?>? {
    for (y in searchRange(yStart)){
        val monochrome = rowMonochrome(y, sampleStep)
        if (monochrome == null || absMeanDifference(monochrome, nonCropAreaMonochrome) > MONOCHROME_THRESHOLD)
            return y to monochrome
    }
    return null
}

private fun Bitmap.getBottomEdge(yStart: Int,
                                 sampleStep: Int,
                                 cropAreaMonochrome: ColorVector?): Pair<Int, ColorVector?> {
    for (y in searchRange(yStart)){
        val monochrome = rowMonochrome(y, sampleStep)
        if (monochrome != null && (cropAreaMonochrome == null || absMeanDifference(monochrome, cropAreaMonochrome) > MONOCHROME_THRESHOLD))
            return y to monochrome
    }
    return height - 1 to null
}

private fun Bitmap.rowMonochrome(y: Int, sampleStep: Int): ColorVector?{
    val colorVectors = mutableListOf(ColorVector(getPixel(0, y)))
    (sampleStep until width step sampleStep).forEachIndexed { i, x ->
        val vector = ColorVector(getPixel(x, y))
        if (absMeanDifference(vector, colorVectors[i]) > INTRA_ROW_FLUCTUATION_THRESHOLD)
            return null
        colorVectors.add(vector)
    }
    return colorVectors.iterator().mean()
}

private fun Bitmap.searchRange(yStart: Int): IntRange =
    (yStart until  height)