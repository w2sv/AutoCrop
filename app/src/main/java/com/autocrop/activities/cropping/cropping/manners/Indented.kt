package com.autocrop.activities.cropping.cropping.manners

import android.graphics.Bitmap
import timber.log.Timber
import kotlin.math.max
import kotlin.math.roundToInt


private data class IndentedCropBorders(
    val x: BorderPair,
    val y: BorderPair
){ fun nEnclosedPixels(): Int = x.difference() * y.difference() }


private typealias IndentedCropBordersList = List<IndentedCropBorders>
private typealias IndentedCropBordersCandidates = MutableList<IndentedCropBorders>

private const val N_BORDER_CONFIRMATION_PIXEL_COMPARISONS: Int = 10


fun indentedlyCroppedImage(image: Bitmap): BitmapWithRetentionPercentage?{
    val borderCandidates: IndentedCropBordersList = indentedCropBorderCandidates(image, image.height - 1)
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
                x.first,
                y.first,
                x.difference(),
                y.difference()),
            (nEnclosedPixels().toFloat() / (image.width * image.height).toFloat() * 100f).roundToInt()
        )
    }
}


private fun indentedCropBorderCandidates(image: Bitmap, lastRowIndex: Int): IndentedCropBordersList {
    val centerIndex: Int = image.width / 2

    fun findUpperBorder(yStart: Int, candidates: IndentedCropBordersCandidates): IndentedCropBordersCandidates {
        fun findY2(y1: Int, xBorderCandidate: BorderPair, candidates: IndentedCropBordersCandidates): IndentedCropBordersCandidates {
            fun confirmUpperBorder(
                xBorderCandidate: BorderPair,
                yBorderCandidate: BorderPair,
                candidates: IndentedCropBordersCandidates
            ): IndentedCropBordersCandidates {

                if (xBorderCandidate.confirmedX(image, yBorderCandidate.second)){
                    candidates.add(
                        IndentedCropBorders(
                            xBorderCandidate,
                            yBorderCandidate
                        )
                    )
                }
                else
                    Timber.i("Couldn't confirm upper border")

                return findUpperBorder(yBorderCandidate.second + 1, candidates)
            }

            var y2: Int = y1
            while (image.getPixel(
                    xBorderCandidate.first,
                    y2
                ) != image.getPixel(xBorderCandidate.first, y2 + 1)
            )
                y2 += 1
            if (y2 != y1) {
                // confirm found y2
                with (Pair(y1, y2)){
                    if (confirmedY(image, xBorderCandidate.second))
                        return confirmUpperBorder(xBorderCandidate, this, candidates)
                    Timber.i("Couldn't confirm y2")
                }
            }
            return findUpperBorder(y1 + 1, candidates)
        }

        for (y1 in yStart until lastRowIndex){
            var x1: Int = centerIndex
            while(x1 >= 1 && image.hasNoFluctuationOverXSubsequentPixelsInCurrentRowAndFluctuationOverOnesInRowBeneath(x1, y1, Int::minus))
                x1 -= 1
            if (x1 != centerIndex){
                var x2: Int = centerIndex
                while(x2 <= image.width - 2 && image.hasNoFluctuationOverXSubsequentPixelsInCurrentRowAndFluctuationOverOnesInRowBeneath(x2, y1, Int::plus))
                    x2 += 1
                if (x2 != centerIndex)
                    return findY2(y1, Pair(x1, x2), candidates)
            }
        }
        return candidates
    }

    return findUpperBorder(0, mutableListOf())
        .toList()
}


private fun Bitmap.hasNoFluctuationOverXSubsequentPixelsInCurrentRowAndFluctuationOverOnesInRowBeneath(
    xPreceding: Int,
    y: Int,
    stepOperation: (Int, Int) -> Int
): Boolean =
    stepOperation(xPreceding, 1).run {
        getPixel(xPreceding, y) == getPixel(this, y) && getPixel(xPreceding, y + 1) != getPixel(this, y + 1)
    }


private fun BorderPair.confirmedX(image: Bitmap, y: Int): Boolean{
    val step: Int = max(difference() / N_BORDER_CONFIRMATION_PIXEL_COMPARISONS, 1)
    return (
            (first + step until second step step).all {
                image.getPixel(it - step, y) != image.getPixel(it, y)
            }
            )
}


private fun BorderPair.confirmedY(image: Bitmap, x: Int): Boolean{
    val step: Int = max(difference() / N_BORDER_CONFIRMATION_PIXEL_COMPARISONS, 1)
    return (
            (first + step until second step step).all {
                image.getPixel(x, it - step) == image.getPixel(x, it)
            }
            )
}