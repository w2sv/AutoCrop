package com.autocrop.activities.cropping

import android.graphics.Bitmap
import kotlin.math.roundToInt


private typealias BorderPair = Pair<Int, Int>
private typealias BorderPairs = List<BorderPair>

private const val N_PIXEL_COMPARISONS_PER_ROW: Int = 5
private const val N_PIXEL_COMPARISONS_PER_COLUMN: Int = 4


fun croppedImage(image: Bitmap): Pair<Bitmap, Int>?{
    val lastRowIndex: Int = image.height - 1

    var borderPairCandidates: BorderPairs = getCroppingBorderPairCandidates(image, lastRowIndex)

    // discard cropping borders limiting crop with non-fluctuating columns in horizontal center vicinity
    // if more than one border pair candidate found
    if (borderPairCandidates.size > 1)
        borderPairCandidates = borderPairCandidates.filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image)

    if (borderPairCandidates.isEmpty())
        return null

    // find cropping border pair of maximal crop height
    val croppingBorders: BorderPair = borderPairCandidates.maxBy {
        it.second - it.first
    }!!

    val y: Int = croppingBorders.first + 1
    val height: Int = croppingBorders.second - 2 - y

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

    fun getCropStartInd(queryStartInd: Int){
        fun getCropEndInd(borderStartInd: Int){
            for (i in borderStartInd until lastRowIndex - 1){
                if (image.hasFluctuationThroughoutRow(i, sampleStep) && !image.hasFluctuationThroughoutRow(i + 1, sampleStep)){
                    croppingBorderPairCandidates.add(
                        BorderPair(
                            borderStartInd,
                            i
                        )
                    )
                    return getCropStartInd(i+1)
                }
            }
            croppingBorderPairCandidates.add(
                BorderPair(
                    borderStartInd,
                    lastRowIndex
                )
            )
        }

        for (i in queryStartInd until lastRowIndex - 1){
            if (!image.hasFluctuationThroughoutRow(i, sampleStep) && image.hasFluctuationThroughoutRow(i + 1, sampleStep))
                return getCropEndInd(i)
        }
    }

    getCropStartInd(0)
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