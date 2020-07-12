package com.bunsenbrenner.screenshotboundremoval

import android.graphics.Bitmap

/*
 * x -> column index
 * y -> row index
 */

fun Bitmap.hasFluctuationThroughoutRow(y: Int, sampleStep: Int): Boolean = !(sampleStep until this.width-1 step sampleStep).all { this.getPixel(it, y) == this.getPixel(it - sampleStep, y) }

fun Bitmap.hasFluctuationThrougoutColumn(x: Int, y: Int, candidateHeight: Int): Boolean{
    val step: Int = (candidateHeight + y) / 4
    return !(y + step until candidateHeight + y step step).all { this.getPixel(x, it) == this.getPixel(x, it - step)}
}