package com.example.screenshotboundremoval

import android.graphics.Bitmap

private typealias BorderPair = Pair<Int, Int>

class Cropper(private val image: Bitmap){
    companion object{
        private const val pixelComparisonsPerRow: Int = 5
    }
    private val width: Int = image.width
    private val height: Int = image.height
    private val sampleStep: Int = width / pixelComparisonsPerRow

    private val borderPairs: MutableList<BorderPair> = mutableListOf()

    private fun getStartInd(queryStartInd: Int){
        for (i in queryStartInd until height-2){
            if (!image.hasFluctuationThroughoutRow(i, sampleStep) && image.hasFluctuationThroughoutRow(i+1, sampleStep))
                return getEndInd(i+1)
        }
        borderPairs.add(BorderPair(queryStartInd, height-1))
    }

    private fun getEndInd(queryStartInd: Int){
        for (i in queryStartInd until height-1){
            if (image.hasFluctuationThroughoutRow(i, sampleStep) && !image.hasFluctuationThroughoutRow(i+1, sampleStep)){
                borderPairs.add(BorderPair(queryStartInd, i))
                return getStartInd(i+1)
            }
        }
        borderPairs.add(BorderPair(queryStartInd, height-1))
    }

    // private fun isValid(croppingBounds: BorderPair): Boolean = croppingBounds != BorderPair(0, height) && (croppingBounds.second - croppingBounds.first).toFloat() / height.toFloat() > 0.15
    fun getCroppedImage(): Bitmap{
        getStartInd(0)
        val croppingBorders: BorderPair = borderPairs.maxBy { it.second - it.first }!!
        // valid = isValid(croppingBorders)

        return Bitmap.createBitmap(image, 0, croppingBorders.first, width, croppingBorders.second - croppingBorders.first)
    }
}