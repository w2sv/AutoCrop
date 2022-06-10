package com.autocrop.activities.cropping.fragments.cropping

import android.graphics.Bitmap
import android.graphics.Rect

private data class VerticalEdges(val top: Int, val bottom: Int){
    constructor(values: Pair<Int, Int>):
        this(values.first, values.second)

    fun height(): Int =
        bottom - top
}

fun Bitmap.cropped(rect: Rect): Bitmap =
    Bitmap.createBitmap(
        this,0,
        rect.top,
        width,
        rect.height()
    )

fun cropRect(screenshot: Bitmap): Rect?{
    val borderPairCandidates: List<VerticalEdges> = cropEdgesCandidates(screenshot)
        .filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(screenshot)

    if (borderPairCandidates.isEmpty())
        return null

    val (topEdge: Int, bottomEdge: Int) = maxHeightCropEdges(borderPairCandidates)

    return Rect(0, topEdge, screenshot.width, bottomEdge)
}

private fun maxHeightCropEdges(borderCandidates: List<VerticalEdges>): VerticalEdges{
    val margin = 1

    return VerticalEdges(
        borderCandidates.maxByOrNull {it.height()}!!.let { maxSizeBorderPair ->
            (maxSizeBorderPair.top + margin) to maxSizeBorderPair.bottom - margin
        }
    )
}

private fun cropEdgesCandidates(image: Bitmap): List<VerticalEdges> {
    val nPixelComparisonsPerRow = 5

    val lastRowIndex = image.height - 1

    val sampleStep: Int = image.width / nPixelComparisonsPerRow
    fun searchRange(startIndex: Int): IntRange =
        (startIndex until lastRowIndex)

    fun getTopEdge(queryStartInd: Int, candidates: MutableList<VerticalEdges>): MutableList<VerticalEdges> {
        fun getBottomEdge(lowerBoundIndex: Int, candidates: MutableList<VerticalEdges>): MutableList<VerticalEdges> {
            var precedingRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(lowerBoundIndex, sampleStep)

            for (i in searchRange(lowerBoundIndex + 1)){
                val currentRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(i, sampleStep)

                if (precedingRowHasFluctuation && !currentRowHasFluctuation){
                    candidates.add(VerticalEdges(lowerBoundIndex, i))
                    return getTopEdge(i + 1, candidates)
                }
                precedingRowHasFluctuation = currentRowHasFluctuation
            }

            candidates.add(VerticalEdges(lowerBoundIndex, lastRowIndex))
            return candidates
        }

        var precedingRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(queryStartInd, sampleStep)

        for (i in searchRange(queryStartInd + 1)){
            val currentRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(i + 1, sampleStep)

            if (!precedingRowHasFluctuation && currentRowHasFluctuation)
                return getBottomEdge(i + 1, candidates)
            precedingRowHasFluctuation = currentRowHasFluctuation
        }

        return candidates
    }

    return getTopEdge(0, mutableListOf())
        .toList()
}

/**
 * x -> column index
 * y -> row index
 */
private fun Bitmap.hasFluctuationThroughoutRow(y: Int, sampleStep: Int): Boolean =
    (sampleStep until width step sampleStep).any {
         getPixel(it, y) != getPixel(it - sampleStep, y)
    }

private fun List<VerticalEdges>.filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image: Bitmap): List<VerticalEdges> {
    val widthQueryOffsetPercentage = 0.4f
    val horizontalQueryOffset: Int = (image.width * widthQueryOffsetPercentage).toInt()

    return filter{ verticalEdges ->
        (horizontalQueryOffset..image.width-horizontalQueryOffset)
            .all{
                image.hasFluctuationThroughoutColumn(it, verticalEdges.top, verticalEdges.height())
            }
        }
}

private fun Bitmap.hasFluctuationThroughoutColumn(x: Int, y: Int, candidateHeight: Int): Boolean{
    val nPixelComparisonsPerColumn = 4
    val step: Int = (candidateHeight + y) / nPixelComparisonsPerColumn

    return (y + step until candidateHeight + y step step).any {
        getPixel(x, it) != getPixel(x, it - step)
    }
}