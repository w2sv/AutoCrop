package com.autocrop.activities.cropping.fragments.cropping

import android.graphics.Bitmap
import kotlin.math.roundToInt

private typealias BorderPair = Pair<Int, Int>
private typealias BorderPairs = List<BorderPair>
private typealias BorderPairCandidates = MutableList<BorderPair>

fun croppedImage(image: Bitmap): Triple<Bitmap, Int, Int>?{
    val borderPairCandidates: BorderPairs = getCroppingBorderPairCandidates(image, image.height - 1)
        .filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image)
        .also { if (it.isEmpty())
            return null
        }

    // find cropping border pair of maximal crop height
    val upperBoundMargin = 2
    val (y: Int, height: Int) = borderPairCandidates.maxByOrNull {it.second - it.first}!!.let { maxSizeBorderPair ->
        (maxSizeBorderPair.first + 1).let { y ->
            y to maxSizeBorderPair.second - y - upperBoundMargin
        }
    }
    val discardedPercentage: Float = image.height.toFloat().run {
        minus(height.toFloat()) / this
    }

    return Triple(
        Bitmap.createBitmap(image,0, y, image.width, height),
        (discardedPercentage * 100).roundToInt(),
        (discardedPercentage * image.approximatedJpegFileSizeInKB).roundToInt()
    )
}

private val Bitmap.approximatedJpegFileSizeInKB: Int
    get() = allocationByteCount / 10 / 1024

private fun getCroppingBorderPairCandidates(image: Bitmap, lastRowIndex: Int): BorderPairs {
    val nPixelComparisonsPerRow = 5

    val sampleStep: Int = image.width / nPixelComparisonsPerRow
    fun searchRange(startIndex: Int): IntRange = (startIndex until lastRowIndex)

    fun getLowerBoundIndex(queryStartInd: Int, candidates: BorderPairCandidates = mutableListOf()): BorderPairCandidates {
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

    return getLowerBoundIndex(0)
        .toList()
}

/**
 * x -> column index
 * y -> row index
 */
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
    val nPixelComparisonsPerColumn = 4
    val step: Int = (candidateHeight + y) / nPixelComparisonsPerColumn

    return !(y + step until candidateHeight + y step step).all {
        getPixel(x, it) == getPixel(x, it - step)
    }
}