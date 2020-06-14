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
                return getEndInd(i)
        }
        borderPairs.add(BorderPair(queryStartInd, height-1))
    }

    private fun getEndInd(borderStartInd: Int){
        for (i in borderStartInd until height-2){
            if (image.hasFluctuationThroughoutRow(i, sampleStep) && !image.hasFluctuationThroughoutRow(i+1, sampleStep)){
                borderPairs.add(BorderPair(borderStartInd, i))
                return getStartInd(i+1)
            }
        }
        borderPairs.add(BorderPair(borderStartInd, height-1))
    }

    fun getCroppedImage(): Bitmap{
        getStartInd(0)
        val croppingBorders: BorderPair = borderPairs.maxBy { it.second - it.first }!!
        return Bitmap.createBitmap(image, 0, croppingBorders.first, width, croppingBorders.second - croppingBorders.first)
    }
}