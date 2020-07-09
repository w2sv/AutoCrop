package com.example.screenshotboundremoval

import android.graphics.Bitmap

private typealias BorderPair = Pair<Int, Int>
private const val PIXEL_COMPARISONS_PER_ROW: Int = 5

class Cropper(private val image: Bitmap){

    private val width: Int = image.width
    private val lastRowInd: Int = image.height - 1
    private val sampleStep: Int = width / PIXEL_COMPARISONS_PER_ROW

    private val croppingBoundPairCandidates: MutableList<BorderPair> = mutableListOf()

    private fun getStartInd(queryStartInd: Int){
        for (i in queryStartInd until lastRowInd-1){
            if (!image.hasFluctuationThroughoutRow(i, sampleStep) && image.hasFluctuationThroughoutRow(i+1, sampleStep))
                return getEndInd(i)
        }
        croppingBoundPairCandidates.add(BorderPair(queryStartInd, lastRowInd))
    }

    private fun getEndInd(borderStartInd: Int){
        for (i in borderStartInd until lastRowInd-1){
            if (image.hasFluctuationThroughoutRow(i, sampleStep) && !image.hasFluctuationThroughoutRow(i+1, sampleStep)){
                croppingBoundPairCandidates.add(BorderPair(borderStartInd, i))
                return getStartInd(i+1)
            }
        }
        croppingBoundPairCandidates.add(BorderPair(borderStartInd, lastRowInd))
    }

    private fun filterInCenterProximityVerticallyFluctuatingBorderPairs(): List<BorderPair> {
        val WIDTH_QUERY_OFFSET_PERCENTAGE: Float = 0.4.toFloat()

        val horizontalQueryOffset: Int = (width * WIDTH_QUERY_OFFSET_PERCENTAGE).toInt()
        return croppingBoundPairCandidates.filter{ borderPair -> (horizontalQueryOffset..width-horizontalQueryOffset).all{ image.hasFluctuationThrougoutColumn(it, borderPair.first, borderPair.second - borderPair.first) } }
    }

    fun getCroppedImage(): Bitmap?{
        getStartInd(0)
        var inCenterProximityVerticallyFluctuatingBorderPairs: List<BorderPair>? =  null
        if (croppingBoundPairCandidates.size > 1)
            inCenterProximityVerticallyFluctuatingBorderPairs = filterInCenterProximityVerticallyFluctuatingBorderPairs()
        val finalCroppingBorderCandidates = if(!inCenterProximityVerticallyFluctuatingBorderPairs.isNullOrEmpty()) inCenterProximityVerticallyFluctuatingBorderPairs else croppingBoundPairCandidates
        val croppingBorders: BorderPair = finalCroppingBorderCandidates.maxBy { it.second - it.first }!!
        val validCrop: Boolean = croppingBorders != Pair(0, lastRowInd)
        return if(validCrop) Bitmap.createBitmap(
            image,
            0,
            croppingBorders.first + 1,
            width,
            croppingBorders.second - 2 - croppingBorders.first + 1)
                else null
    }
}