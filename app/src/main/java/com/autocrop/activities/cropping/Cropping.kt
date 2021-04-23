package com.autocrop.activities.cropping

import android.graphics.Bitmap
import kotlin.math.roundToInt


private typealias BorderPair = Pair<Int, Int>
private typealias BorderPairs = List<BorderPair>

private const val N_PIXEL_COMPARISONS_PER_ROW: Int = 5
private const val N_PIXEL_COMPARISONS_PER_COLUMN: Int = 4

private const val UPPER_BOUND_MARGIN: Int = -1


fun croppedImage(image: Bitmap): Pair<Bitmap, Int>?{
    val lastRowIndex: Int = image.height - 1

    val borderPairCandidates: BorderPairs = getCroppingBorderPairCandidates(image, lastRowIndex)
        .filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image)
        .also { if (it.isEmpty())
            return null
        }

    // find cropping border pair of maximal crop height
    val croppingBorders: BorderPair = borderPairCandidates.maxBy {
        it.second - it.first
    }!!

    val y: Int = croppingBorders.first
    val height: Int = croppingBorders.second - y + UPPER_BOUND_MARGIN

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

    val croppingBorderPairCandidates: MutableList<BorderPair> = mutableListOf()

    fun getLowerBoundIndex(queryStartInd: Int){
        fun getUpperBoundIndex(lowerBoundIndex: Int){
            var precedingRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(lowerBoundIndex, sampleStep)

            for (i in lowerBoundIndex + 1 until lastRowIndex - 1){
                val currentRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(i, sampleStep)

                if (precedingRowHasFluctuation && !currentRowHasFluctuation){
                    croppingBorderPairCandidates.add(
                        BorderPair(
                            lowerBoundIndex,
                            i
                        )
                    )
                    return getLowerBoundIndex(i + 1)
                }
                precedingRowHasFluctuation = currentRowHasFluctuation
            }

            croppingBorderPairCandidates.add(
                BorderPair(
                    lowerBoundIndex,
                    lastRowIndex
                )
            )
        }

        var precedingRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(queryStartInd, sampleStep)

        for (i in queryStartInd + 1 until lastRowIndex - 1){
            val currentRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(i + 1, sampleStep)

            if (!precedingRowHasFluctuation && currentRowHasFluctuation)
                return getUpperBoundIndex(i + 1)
            precedingRowHasFluctuation = currentRowHasFluctuation
        }
    }

    getLowerBoundIndex(0)
    return croppingBorderPairCandidates.toList()
}

 /*
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
    val step: Int = (candidateHeight + y) / N_PIXEL_COMPARISONS_PER_COLUMN

    return !(y + step until candidateHeight + y step step).all {
        getPixel(x, it) == getPixel(x, it - step)
    }
}