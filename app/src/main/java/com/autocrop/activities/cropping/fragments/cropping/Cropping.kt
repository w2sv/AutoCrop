package com.autocrop.activities.cropping.fragments.cropping

import android.graphics.Bitmap
import android.graphics.Rect

private typealias Edges = Pair<Int, Int>  // TODO
val Edges.top: Int get() = first
val Edges.bottom: Int get() = second

fun cropRect(screenshot: Bitmap): Rect?{
    val borderPairCandidates: List<Edges> = borderPairCandidates(screenshot)
        .filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(screenshot)
        .also { if (it.isEmpty())
            return null
        }

    val (topEdge: Int, bottomEdge: Int) = maxHeightCropEdges(borderPairCandidates)

    return Rect(0, topEdge, screenshot.width, bottomEdge)
}

fun maxHeightCropEdges(borderCandidates: List<Edges>): Edges =
    borderCandidates.maxByOrNull {it.bottom - it.top}!!.let { maxSizeBorderPair ->
        (maxSizeBorderPair.top + 1) to maxSizeBorderPair.bottom - 2
    }

private fun borderPairCandidates(image: Bitmap): List<Edges> {
    val nPixelComparisonsPerRow = 5

    val lastRowIndex = image.height - 1

    val sampleStep: Int = image.width / nPixelComparisonsPerRow
    fun searchRange(startIndex: Int): IntRange = (startIndex until lastRowIndex)

    fun getTopEdge(queryStartInd: Int, candidates: MutableList<Edges>): MutableList<Edges> {
        fun getBottomEdge(lowerBoundIndex: Int, candidates: MutableList<Edges>): MutableList<Edges> {
            var precedingRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(lowerBoundIndex, sampleStep)

            for (i in searchRange(lowerBoundIndex + 1)){
                val currentRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(i, sampleStep)

                if (precedingRowHasFluctuation && !currentRowHasFluctuation){
                    candidates.add(lowerBoundIndex to i)
                    return getTopEdge(i + 1, candidates)
                }
                precedingRowHasFluctuation = currentRowHasFluctuation
            }

            candidates.add(lowerBoundIndex to lastRowIndex)
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
private fun Bitmap.hasFluctuationThroughoutRow(y: Int, sampleStep: Int): Boolean = !(sampleStep until width - 1 step sampleStep).all {
     getPixel(it, y) == getPixel(it - sampleStep, y)
}

private fun List<Edges>.filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image: Bitmap): List<Edges> {
    val widthQueryOffsetPercentage = 0.4f
    val horizontalQueryOffset: Int = (image.width * widthQueryOffsetPercentage).toInt()

    return filter{
                borderPair -> (horizontalQueryOffset..image.width-horizontalQueryOffset)
            .all{
                image.hasFluctuationThroughoutColumn(it, borderPair.first, borderPair.second - borderPair.first)
            }
        }
}

private fun Bitmap.hasFluctuationThroughoutColumn(x: Int, y: Int, candidateHeight: Int): Boolean{
    val nPixelComparisonsPerColumn = 4
    val step: Int = (candidateHeight + y) / nPixelComparisonsPerColumn

    return !(y + step until candidateHeight + y step step).all {
        getPixel(x, it) == getPixel(x, it - step)
    }
}