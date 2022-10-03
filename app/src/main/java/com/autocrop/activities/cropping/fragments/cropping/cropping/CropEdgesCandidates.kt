package com.autocrop.activities.cropping.fragments.cropping.cropping

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

fun Bitmap.cropEdgesCandidates(pixelComparisonsPerRow: Int): List<VerticalEdges> =
    getTopEdge(0, width / pixelComparisonsPerRow, mutableListOf())
        .toList()

private fun Bitmap.getTopEdge(queryStartInd: Int, sampleStep: Int, candidates: MutableList<VerticalEdges>): MutableList<VerticalEdges> {
    var precedingRowHasFluctuation: Boolean = hasFluctuationThroughoutRow(queryStartInd, sampleStep)

    for (i in searchRange(queryStartInd + 1)){
        val currentRowHasFluctuation: Boolean = hasFluctuationThroughoutRow(i + 1, sampleStep)

        if (!precedingRowHasFluctuation && currentRowHasFluctuation)
            return getBottomEdge(i + 1, sampleStep, candidates)
        precedingRowHasFluctuation = currentRowHasFluctuation
    }

    return candidates
}

private fun Bitmap.getBottomEdge(lowerBoundIndex: Int, sampleStep: Int, candidates: MutableList<VerticalEdges>): MutableList<VerticalEdges> {
    var precedingRowHasFluctuation: Boolean = hasFluctuationThroughoutRow(lowerBoundIndex, sampleStep)

    for (i in searchRange(lowerBoundIndex + 1)){
        val currentRowHasFluctuation: Boolean = hasFluctuationThroughoutRow(i, sampleStep)

        if (precedingRowHasFluctuation && !currentRowHasFluctuation){
            candidates.add(VerticalEdges(lowerBoundIndex, i))
            return getTopEdge(i + 1, sampleStep, candidates)
        }
        precedingRowHasFluctuation = currentRowHasFluctuation
    }

    return candidates.apply {
        add(VerticalEdges(lowerBoundIndex, lastRowIndex))
    }
}

/**
 * x -> column index
 * y -> row index
 */
private fun Bitmap.hasFluctuationThroughoutRow(y: Int, sampleStep: Int, threshold: Float = 0.015f): Boolean =
    (sampleStep until width step sampleStep).any {
        meanDifference(Color.valueOf(getPixel(it, y)), Color.valueOf(getPixel(it - sampleStep, y))) > threshold
    }

private fun meanDifference(a: Color, b: Color): Float =
    (abs(a.alpha() - b.alpha()) +
    abs(a.red() - b.red()) +
    abs(a.green() - b.green()) +
    abs(a.blue() - b.blue())) / 4f

private val Bitmap.lastRowIndex: Int
    get() = height - 1

private fun Bitmap.searchRange(startIndex: Int): IntRange =
    (startIndex until lastRowIndex)