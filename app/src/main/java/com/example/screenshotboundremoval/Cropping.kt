package com.example.screenshotboundremoval

import android.graphics.Bitmap
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.pow

typealias BorderPair = Pair<Int, Int>
typealias ImageArray = Array<Row>

class Row(pixelRow: IntArray){
    data class Pixel(val pixel: Int){
        private val colors: Array<Int> = arrayOf(pixel.red, pixel.green, pixel.blue)

        operator fun get(index: Int): Int = colors[index]
    }

    private val row: Array<Pixel> = pixelRow.map { Pixel(it) }.toTypedArray()

    operator fun get(index: Int): Pixel = row[index]

    private fun colorChannelRow(color: Int): IntArray = row.map { it[color] }.toIntArray()
    fun IntArray.std(): Double = this.map { (it - this.average()).pow(2) }.sum() / this.size

    fun fluctuation(): Double = (0..2).map { this.colorChannelRow(it).std() }.sum() / 3
    fun hasFluctuation(): Boolean = this.fluctuation() != 0.toDouble()
}

class Cropper(b: Bitmap?){
    val bitmap: Bitmap = b!!
    val width = bitmap.width
    val height = bitmap.height

    private fun IntArray.toRow(): Row = Row(this)
    val imageArray: ImageArray = (0 until height).map { row -> (0 until width).map {col -> bitmap.getPixel(col, row)}.toIntArray().toRow() }.toTypedArray()

    val borderPairs: MutableList<BorderPair> = mutableListOf()

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

    fun getCroppedBitmap(): Bitmap{
        getBorderPairs()
        val croppingBorders = getCroppingBorders()
        println("original dimensions: $width $height")
        println("cropping indices: $croppingBorders")

        return Bitmap.createBitmap(bitmap, 0, croppingBorders.first, width, croppingBorders.second - croppingBorders.first)
    }
}