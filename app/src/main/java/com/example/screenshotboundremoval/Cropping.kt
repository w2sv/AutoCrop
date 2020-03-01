package com.example.screenshotboundremoval

import android.graphics.Bitmap
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.pow

typealias BorderPair = Pair<Int, Int>
typealias ImageArray = Array<Array<Int>>


class Cropper(b: Bitmap?){
    val bitmap: Bitmap = b!!
    val width = bitmap.width
    val height = bitmap.height

    val sampleStep: Int = 5

    private val imageArray: ImageArray = (0 until height).map { row -> (0 until width).map {col -> bitmap.getPixel(col, row)}.toTypedArray() }.toTypedArray()

    private val borderPairs: MutableList<BorderPair> = mutableListOf()

    private fun Array<Int>.hasFluctuation(): Boolean = !(sampleStep until width step sampleStep).all { this[it] == this [it-sampleStep]}

    private fun getStartInd(queryStartInd: Int){
        for (i in queryStartInd until height-2){
            if (!imageArray[i].hasFluctuation() && imageArray[i+1].hasFluctuation())
                return getEndInd(i+1)
        }
        borderPairs.add(BorderPair(queryStartInd, height-1))
    }

    private fun getEndInd(queryStartInd: Int){
        for (i in queryStartInd until height-1){
            if (imageArray[i].hasFluctuation() && !imageArray[i+1].hasFluctuation()){
                borderPairs.add(BorderPair(queryStartInd, i))
                return getStartInd(i+1)
            }
        }
        borderPairs.add(BorderPair(queryStartInd, height-1))
    }

    private fun getBorderPairs(){
        return getStartInd(0)
    }

    private fun getCroppingBorders(): BorderPair = borderPairs.maxBy { it.second - it.first }!!

    private fun isValid(croppingBounds: BorderPair): Boolean = croppingBounds != BorderPair(0, height) && (croppingBounds.second - croppingBounds.first).toFloat() / height.toFloat() > 0.15

    var valid: Boolean? = null

    fun getCroppedBitmap(): Bitmap{
        getBorderPairs()
        val croppingBorders = getCroppingBorders()
        valid = isValid(croppingBorders)

        println("original dimensions: $width $height")
        println("cropping indices: $croppingBorders")

        return Bitmap.createBitmap(bitmap, 0, croppingBorders.first, width, croppingBorders.second - croppingBorders.first)
    }
}