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
        this,
        0,
        rect.top,
        width,
        rect.height()
    )

fun cropRect(screenshot: Bitmap): Rect?{
    val borderPairCandidates: List<VerticalEdges> = screenshot.cropEdgesCandidates(5)

    if (borderPairCandidates.isEmpty())
        return null

    return maxHeightCropEdges(borderPairCandidates).run {
        Rect(0, top, screenshot.width, bottom)
    }
}

private fun maxHeightCropEdges(borderCandidates: List<VerticalEdges>): VerticalEdges{
    val margin = 1

    return VerticalEdges(
        borderCandidates.maxByOrNull {it.height()}!!.let { maxSizeBorderPair ->
            (maxSizeBorderPair.top + margin) to maxSizeBorderPair.bottom - margin
        }
    )
}

private fun Bitmap.cropEdgesCandidates(pixelComparisonsPerRow: Int): List<VerticalEdges> {
    return getTopEdge(0, width / pixelComparisonsPerRow, mutableListOf())
        .toList()
}

private val Bitmap.lastRowIndex: Int
    get() = height - 1

private fun Bitmap.searchRange(startIndex: Int): IntRange =
    (startIndex until lastRowIndex)

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
private fun Bitmap.hasFluctuationThroughoutRow(y: Int, sampleStep: Int): Boolean =
    (sampleStep until width step sampleStep).any {
         getPixel(it, y) != getPixel(it - sampleStep, y)
    }

//private fun List<VerticalEdges>.filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image: Bitmap): List<VerticalEdges> {
//    val widthQueryOffsetPercentage = 0.4f
//    val horizontalQueryOffset: Int = (image.width * widthQueryOffsetPercentage).toInt()
//
//    return filter{ verticalEdges ->
//        (horizontalQueryOffset..image.width-horizontalQueryOffset)
//            .all{
//                image.hasFluctuationThroughoutColumn(it, verticalEdges.top, verticalEdges.height())
//            }
//        }
//}
//
//private fun Bitmap.hasFluctuationThroughoutColumn(x: Int, y: Int, candidateHeight: Int): Boolean{
//    val nPixelComparisonsPerColumn = 4
//    val step: Int = (candidateHeight + y) / nPixelComparisonsPerColumn
//
//    return (y + step until candidateHeight + y step step).any {
//        getPixel(x, it) != getPixel(x, it - step)
//    }
//}