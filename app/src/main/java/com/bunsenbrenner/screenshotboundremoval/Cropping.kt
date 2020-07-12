package com.bunsenbrenner.screenshotboundremoval

import android.graphics.Bitmap

private typealias BorderPair = Pair<Int, Int>
private const val PIXEL_COMPARISONS_PER_ROW: Int = 5

class Cropper(private val image: Bitmap){

    private val width: Int = image.width
    private val lastRowInd: Int = image.height - 1
    private val sampleStep: Int = width / PIXEL_COMPARISONS_PER_ROW

    private fun getCroppingBorderPairCandidates(): List<BorderPair> {
        val croppingBorderPairCandidates: MutableList<BorderPair> = mutableListOf()

        fun getCropStartInd(queryStartInd: Int){
            fun getCropEndInd(borderStartInd: Int){
                for (i in borderStartInd until lastRowInd-1){
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
                        lastRowInd
                    )
                )
            }

            for (i in queryStartInd until lastRowInd-1){
                if (!image.hasFluctuationThroughoutRow(i, sampleStep) && image.hasFluctuationThroughoutRow(i+1, sampleStep))
                    return getCropEndInd(i)
            }
            croppingBorderPairCandidates.add(
                BorderPair(
                    queryStartInd,
                    lastRowInd
                )
            )
        }

        getCropStartInd(0)
        return croppingBorderPairCandidates.toList()
    }


    private fun filterInCenterProximityVerticallyFluctuatingBorderPairs(croppingBorderPairCandidates: List<BorderPair>): List<BorderPair> {
        val WIDTH_QUERY_OFFSET_PERCENTAGE: Float = 0.4.toFloat()

        val horizontalQueryOffset: Int = (width * WIDTH_QUERY_OFFSET_PERCENTAGE).toInt()
        return croppingBorderPairCandidates.filter{ borderPair -> (horizontalQueryOffset..width-horizontalQueryOffset).all{ image.hasFluctuationThrougoutColumn(it, borderPair.first, borderPair.second - borderPair.first) } }
    }

    fun getCroppedImage(): Bitmap?{
        // get raw border pair candidates
        val borderPairCandidates: List<BorderPair> = getCroppingBorderPairCandidates()

        // discard cropping borders limiting crop with non-fluctuating columns in horizontal center vicinity
        // if more than one border pair candidate found
        var filteredCandidates: List<BorderPair>? =  null
        if (borderPairCandidates.size > 1)
            filteredCandidates = filterInCenterProximityVerticallyFluctuatingBorderPairs(borderPairCandidates)
        val finalCroppingBorderCandidates = if(!filteredCandidates.isNullOrEmpty()) filteredCandidates else borderPairCandidates

        // find cropping border pair of maximal crop height
        val croppingBorders: BorderPair = finalCroppingBorderCandidates.maxBy { it.second - it.first }!!
        val validCrop: Boolean = croppingBorders != Pair(0, lastRowInd)

        // return null if image to be returned equaling original one
        return if(validCrop) Bitmap.createBitmap(
            image,
            0,
            croppingBorders.first + 1,
            width,
            croppingBorders.second - 2 - croppingBorders.first + 1)
                else null
    }
}