package com.autocrop.activities.cropping.cropping.manners

import android.graphics.Bitmap
import timber.log.Timber
import kotlin.math.max
import kotlin.math.roundToInt


data class BorderQuadruple(
    val xBorders: BorderPair,
    val yBorders: BorderPair
){
    fun nEnclosedPixels(): Int = xBorders.difference() * yBorders.difference()
}
typealias BorderQuadruples = List<BorderQuadruple>
typealias BorderQuadrupleCandidates = MutableList<BorderQuadruple>

const val N_BORDER_CONFIRMATION_PIXEL_COMPARISONS: Int = 10


fun indentedlyCroppedImage(image: Bitmap): BitmapWithRetentionPercentage?{
    val borderCandidates: BorderQuadruples = borderQuadrupleCandidates(image, image.height - 1)
        .also {
            Timber.i("Border candidates: $it")
            if (it.isEmpty())
                return null
        }

    return borderCandidates.maxBy {
        it.nEnclosedPixels()
    }!!.run {
        Pair(
            Bitmap.createBitmap(
                image,
                xBorders.first,
                yBorders.first,
                xBorders.difference(),
                yBorders.difference()),
            (nEnclosedPixels().toFloat() / (image.width * image.height).toFloat() * 100f).roundToInt()
        )
    }
}


private fun borderQuadrupleCandidates(image: Bitmap, lastRowIndex: Int): BorderQuadruples {
    val centerIndex: Int = image.width / 2
    Timber.i("Center index: $centerIndex")

    fun findUpperBorder(yStart: Int, candidates: BorderQuadrupleCandidates): BorderQuadrupleCandidates {
        fun findY2(y1: Int, xBorderCandidate: BorderPair, candidates: BorderQuadrupleCandidates): BorderQuadrupleCandidates {
            fun confirmUpperBorder(
                xBorderCandidate: BorderPair,
                yBorderCandidate: BorderPair,
                candidates: BorderQuadrupleCandidates
            ): BorderQuadrupleCandidates {
//                val step: Int = max(xBorderCandidate.difference() / N_BORDER_CONFIRMATION_PIXEL_COMPARISONS, 1)
//                if ((xBorderCandidate.first + step until xBorderCandidate.second step step).all {
//                        image.getPixel(it - step, yBorderCandidate.second) != image.getPixel(it, yBorderCandidate.second)
//                    })
                    candidates.add(
                        BorderQuadruple(
                            xBorderCandidate,
                            yBorderCandidate
                        )
                    )
                return findUpperBorder(yBorderCandidate.second + 1, candidates)
            }

            for (y2 in y1 until lastRowIndex - 1){
                if (image.getPixel(xBorderCandidate.first, y2) != image.getPixel(xBorderCandidate.first, y2 + 1)){
                    if (y2 == y1)
                        return findUpperBorder(y2 + 1, candidates)

                    // confirm
//                    val step: Int = max((y2 - y1) / N_BORDER_CONFIRMATION_PIXEL_COMPARISONS, 1)
//                    if ((y2 + step until y2 step step).all{
//                            image.getPixel(xBorderCandidate.second, it - step) == image.getPixel(xBorderCandidate.second, it)
//                        })
                        return confirmUpperBorder(xBorderCandidate, Pair(y1, y2), candidates)
                    // return findUpperBorder(y1 + 1, candidates)
                }
            }
            return candidates.apply {
                add(
                    BorderQuadruple(
                        xBorderCandidate,
                        Pair(y1, lastRowIndex)
                    )
                )
            }
        }

        for (y in yStart..lastRowIndex){
            for (x1 in centerIndex downTo 0){
                if (!image.hasNoFluctuationOverXSubsequentPixelsInCurrentRowAndFluctuationOverOnesInRowBeneath(y, x1, Int::minus) && x1 != centerIndex){
                    for (x2 in centerIndex until image.width){
                        if (!image.hasNoFluctuationOverXSubsequentPixelsInCurrentRowAndFluctuationOverOnesInRowBeneath(y, x1, Int::plus))
                            return findY2(y + 1, Pair(x1, x2), candidates)
                    }
                }
            }
        }
        return candidates
    }

    return findUpperBorder(0, mutableListOf())
        .toList()
}


private fun Bitmap.hasNoFluctuationOverXSubsequentPixelsInCurrentRowAndFluctuationOverOnesInRowBeneath(
    y: Int,
    x1: Int,
    stepOperation: (Int, Int) -> Int
): Boolean =
    stepOperation(x1, 1).run {
        getPixel(x1, y) == getPixel(this, y) && getPixel(x1, y + 1) != getPixel(this, y + 1)
    }