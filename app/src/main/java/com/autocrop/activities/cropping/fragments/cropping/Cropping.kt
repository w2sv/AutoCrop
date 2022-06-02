package com.autocrop.activities.cropping.fragments.cropping

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import com.autocrop.collections.CropBundle

private typealias Borders = Pair<Int, Int>

fun cropped(screenshot: Bitmap, uri: Uri): CropBundle?{
    val borderPairCandidates: List<Borders> = borderPairCandidates(screenshot)
        .filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(screenshot)
        .also { if (it.isEmpty())
            return null
        }

    val (topEdge: Int, bottomEdge: Int) = maxHeightCropParameters(borderPairCandidates)

    return CropBundle(uri, screenshot, Rect(0, topEdge, screenshot.width, bottomEdge))
}

fun maxHeightCropParameters(borderCandidates: List<Borders>): Pair<Int, Int> =
    borderCandidates.maxByOrNull {it.second - it.first}!!.let { maxSizeBorderPair ->
        (maxSizeBorderPair.first + 1) to maxSizeBorderPair.second - 2
    }

private fun borderPairCandidates(image: Bitmap): List<Borders> {
    val nPixelComparisonsPerRow = 5

    val lastRowIndex = image.height - 1

    val sampleStep: Int = image.width / nPixelComparisonsPerRow
    fun searchRange(startIndex: Int): IntRange = (startIndex until lastRowIndex)

    fun getLowerBoundIndex(queryStartInd: Int, candidates: MutableList<Borders>): MutableList<Borders> {
        fun getUpperBoundIndex(lowerBoundIndex: Int, candidates: MutableList<Borders>): MutableList<Borders> {
            var precedingRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(lowerBoundIndex, sampleStep)

            for (i in searchRange(lowerBoundIndex + 1)){
                val currentRowHasFluctuation: Boolean = image.hasFluctuationThroughoutRow(i, sampleStep)

                if (precedingRowHasFluctuation && !currentRowHasFluctuation){
                    candidates.add(
                        Borders(
                            lowerBoundIndex,
                            i
                        )
                    )
                    return getLowerBoundIndex(i + 1, candidates)
                }
                precedingRowHasFluctuation = currentRowHasFluctuation
            }

            candidates.add(
                Borders(
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

/**
 * x -> column index
 * y -> row index
 */
private fun Bitmap.hasFluctuationThroughoutRow(y: Int, sampleStep: Int): Boolean = !(sampleStep until width - 1 step sampleStep).all {
     getPixel(it, y) == getPixel(it - sampleStep, y)
}

private fun List<Borders>.filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image: Bitmap): List<Borders> {
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