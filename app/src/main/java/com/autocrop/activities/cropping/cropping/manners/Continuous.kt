/*
* x -> column index
* y -> row index
*/


package com.autocrop.activities.cropping.cropping.manners

import android.graphics.Bitmap
import kotlin.math.roundToInt

typealias BorderPair = Pair<Int, Int>
fun BorderPair.difference(): Int = second - first

private typealias BorderPairs = List<BorderPair>
private typealias BorderPairCandidates = MutableList<BorderPair>
typealias BitmapWithRetentionPercentage = Pair<Bitmap, Int>

private const val N_PIXEL_COMPARISONS_PER_ROW: Int = 5
private const val N_PIXEL_COMPARISONS_PER_COLUMN: Int = 4

private const val UPPER_BOUND_MARGIN: Int = 1


fun continuouslyCroppedImage(image: Bitmap): BitmapWithRetentionPercentage?{
    val borderPairCandidates: BorderPairs = getCroppingBorderPairCandidates(image, image.height - 1)
        .filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image)
        .also { if (it.isEmpty())
            return null
        }

    // find cropping border pair of maximal crop height
    val croppingBorders: BorderPair = borderPairCandidates.maxBy {
        it.second - it.first
    }!!

    val y: Int = croppingBorders.first
    val height: Int = croppingBorders.second - y - UPPER_BOUND_MARGIN

    return Pair(
        Bitmap.createBitmap(
            image,
            0,
            y,
            image.width,
            height),
        (height.toFloat() / image.height.toFloat() * 100).roundToInt()
    )
}


private fun getCroppingBorderPairCandidates(image: Bitmap, lastRowIndex: Int): BorderPairs {
    val sampleStep: Int = image.width / N_PIXEL_COMPARISONS_PER_ROW
    fun searchRange(startIndex: Int): IntRange = (startIndex until lastRowIndex)

    fun getLowerBoundIndex(queryStartInd: Int, candidates: BorderPairCandidates): BorderPairCandidates {
        fun getUpperBoundIndex(lowerBoundIndex: Int, candidates: BorderPairCandidates): BorderPairCandidates {
            var precedingRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(lowerBoundIndex, sampleStep)

            for (i in searchRange(lowerBoundIndex + 1)){
                val currentRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(i, sampleStep)

                if (precedingRowHasFluctuation && !currentRowHasFluctuation){
                    candidates.add(
                        BorderPair(
                            lowerBoundIndex,
                            i
                        )
                    )
                    return getLowerBoundIndex(i + 1, candidates)
                }
                precedingRowHasFluctuation = currentRowHasFluctuation
            }

            candidates.add(
                BorderPair(
                    lowerBoundIndex,
                    lastRowIndex
                )
            )
            return candidates
        }

        var precedingRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(queryStartInd, sampleStep)

        for (i in searchRange(queryStartInd + 1)){
            val currentRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(i + 1, sampleStep)

            if (!precedingRowHasFluctuation && currentRowHasFluctuation)
                return getUpperBoundIndex(i + 1, candidates)
            precedingRowHasFluctuation = currentRowHasFluctuation
        }

        return candidates
    }

    return getLowerBoundIndex(0, mutableListOf())
        .toList()
}


private fun Bitmap.hasFluctuationThroughoutRow(y: Int, sampleStep: Int): Boolean = !(sampleStep until width - 1 step sampleStep).all {
    getPixel(it, y) == getPixel(it - sampleStep, y)
}


private fun BorderPairs.filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image: Bitmap): BorderPairs {
    val widthQueryOffsetPercentage = 0.4f

    val horizontalQueryOffset: Int = (image.width * widthQueryOffsetPercentage).toInt()
    return this
        .filter{
                borderPair -> (horizontalQueryOffset..image.width-horizontalQueryOffset)
            .all{
                image.hasFluctuationThroughoutColumn(it, borderPair.first, borderPair.second - borderPair.first)
            }
        }
}


private fun Bitmap.hasFluctuationThroughoutColumn(x: Int, y: Int, candidateHeight: Int): Boolean{
    val step: Int = (candidateHeight + y) / N_PIXEL_COMPARISONS_PER_COLUMN

    return !(y + step until candidateHeight + y step step).all {
        getPixel(x, it) == getPixel(x, it - step)
    }
}