package com.example.screenshotboundremoval

import android.graphics.Bitmap

fun String.replaceMultiple(toBeReplaced: List<String>, replaceWith: String): String = this.run { var copy = this; toBeReplaced.forEach { copy = copy.replace(it, replaceWith) }; copy }

fun Bitmap.hasFluctuationThroughoutRow(rowInd: Int, sampleStep: Int): Boolean = !(sampleStep until this.width-1 step sampleStep).all { this.getPixel(it, rowInd) == this.getPixel(it - sampleStep, rowInd) }
