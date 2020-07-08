package com.example.screenshotboundremoval

import android.graphics.Bitmap

fun Bitmap.hasFluctuationThroughoutRow(rowInd: Int, sampleStep: Int): Boolean = !(sampleStep until this.width-1 step sampleStep).all { this.getPixel(it, rowInd) == this.getPixel(it - sampleStep, rowInd) }
