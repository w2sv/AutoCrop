package com.example.screenshotboundremoval

import android.graphics.Bitmap
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.pow

typealias Color = Array<Int>
typealias BorderPair = Pair<Int, Int>
typealias ImageArray = Array<Row>

class Row(pixelRow: IntArray) {
    private fun pixel2ColorChannels(pixel: Int): Color = arrayOf(pixel.red, pixel.green, pixel.blue)

    private val row: Array<Color> = pixelRow.map { pixel2ColorChannels(it) }.toTypedArray()
    private val length: Int = row.size

    operator fun get(index: Int): Array<Int> = row[index]

    private fun colorChannelRow(color: Int): IntArray = row.map { it[color] }.toIntArray()
    fun IntArray.std(): Double = this.map { (it - this.average()).pow(2) }.sum() / this.size

    fun fluctuation(): Double = (0..2).map { this.colorChannelRow(it).std() }.sum() / 3
    fun hasFluctuation(): Boolean = this.fluctuation() != 0.toDouble()

    /*
     * compares consecutive color channels against each other with certain step size
     * breaks as soon as pixels differing from one another encountered
     */
    fun hasFluctuationDynamically(stepSize: Int): Boolean = !(stepSize until length step stepSize).all { row[it].contentEquals(row[it - stepSize])}

    fun equalColor(color: Color): Boolean = ((0 until length).all { row[it].contentEquals(color) })
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