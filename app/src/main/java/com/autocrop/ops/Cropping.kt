package com.autocrop.ops

import android.graphics.Bitmap
import com.autocrop.CropWithRetentionPercentage
import kotlin.math.roundToInt


private typealias BorderPair = Pair<Int, Int>
private const val PIXEL_COMPARISONS_PER_ROW: Int = 5


fun croppedImage(image: Bitmap): CropWithRetentionPercentage?{
    val lastRowIndex: Int = image.height - 1

    // get raw border pair candidates
    val borderPairCandidates: List<BorderPair> = getCroppingBorderPairCandidates(image, lastRowIndex)

    // discard cropping borders limiting crop with non-fluctuating columns in horizontal center vicinity
    // if more than one border pair candidate found
    var filteredCandidates: List<BorderPair>? =  null
    if (borderPairCandidates.size > 1)
        filteredCandidates = filterInCenterProximityVerticallyFluctuatingBorderPairs(borderPairCandidates, image)
    val finalCroppingBorderCandidates = if(!filteredCandidates.isNullOrEmpty()) filteredCandidates else borderPairCandidates

    // find cropping border pair of maximal crop height
    val croppingBorders: BorderPair = finalCroppingBorderCandidates.maxBy { it.second - it.first }!!
    val validCrop: Boolean = croppingBorders != Pair(0, lastRowIndex)

    // return null if image to be returned equaling original one
    return if(validCrop){
        val y: Int = croppingBorders.first + 1
        val height: Int = croppingBorders.second - 2 - y

        Pair(
            Bitmap.createBitmap(
                image,
                0,
                y,
                image.width,
                croppingBorders.second - 2 - y),
            (height.toFloat() / image.height.toFloat() * 100).roundToInt()
        )
    }
    else null
}


private fun getCroppingBorderPairCandidates(image: Bitmap, lastRowIndex: Int): List<BorderPair> {
    val sampleStep: Int = image.width / PIXEL_COMPARISONS_PER_ROW

    val croppingBorderPairCandidates: MutableList<BorderPair> = mutableListOf()

    fun getCropStartInd(queryStartInd: Int){
        fun getCropEndInd(borderStartInd: Int){
            for (i in borderStartInd until lastRowIndex-1){
                if (image.hasFluctuationThroughoutRow(i, sampleStep) && !image.hasFluctuationThroughoutRow(i+1, sampleStep)){
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
            if (!image.hasFluctuationThroughoutRow(i, sampleStep) && image.hasFluctuationThroughoutRow(i+1, sampleStep))
                return getCropEndInd(i)
        }
        croppingBorderPairCandidates.add(
            BorderPair(
                queryStartInd,
                lastRowIndex
            )
        )
    }

    getCropStartInd(0)
    return croppingBorderPairCandidates.toList()
}

 /*
 * x -> column index
 * y -> row index
 */
private fun Bitmap.hasFluctuationThroughoutRow(y: Int, sampleStep: Int): Boolean = !(sampleStep until this.width-1 step sampleStep).all { this.getPixel(it, y) == this.getPixel(it - sampleStep, y) }

private fun Bitmap.hasFluctuationThroughoutColumn(x: Int, y: Int, candidateHeight: Int): Boolean{
    val step: Int = (candidateHeight + y) / 4
    return !(y + step until candidateHeight + y step step).all { this.getPixel(x, it) == this.getPixel(x, it - step)}
}

private fun filterInCenterProximityVerticallyFluctuatingBorderPairs(croppingBorderPairCandidates: List<BorderPair>, image: Bitmap): List<BorderPair> {
    val WIDTH_QUERY_OFFSET_PERCENTAGE: Float = 0.4.toFloat()

    val horizontalQueryOffset: Int = (image.width * WIDTH_QUERY_OFFSET_PERCENTAGE).toInt()
    return croppingBorderPairCandidates.filter{ borderPair -> (horizontalQueryOffset..image.width-horizontalQueryOffset).all{ image.hasFluctuationThroughoutColumn(it, borderPair.first, borderPair.second - borderPair.first) } }
}