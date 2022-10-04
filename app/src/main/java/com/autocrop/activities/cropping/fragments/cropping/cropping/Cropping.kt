package com.autocrop.activities.cropping.fragments.cropping.cropping

import android.graphics.Bitmap
import android.graphics.Rect

fun Bitmap.cropped(rect: Rect): Bitmap =
    Bitmap.createBitmap(
        this,
        0,
        rect.top,
        width,
        rect.height()
    )

fun cropRect(screenshot: Bitmap): Pair<Rect, List<VerticalEdges>>?{
    val borderPairCandidates: List<VerticalEdges> = screenshot.cropEdgesCandidates(5)

    if (borderPairCandidates.isEmpty())
        return null

    return maxHeightCropEdges(borderPairCandidates).run {
        Rect(0, top, screenshot.width, bottom) to borderPairCandidates
    }
}

private fun maxHeightCropEdges(borderCandidates: List<VerticalEdges>): VerticalEdges{
    val margin = 1

    return VerticalEdges(
        borderCandidates.maxByOrNull {it.height()}!!.let { maxSizeBorderPair ->
            (maxSizeBorderPair.top + margin) to maxSizeBorderPair.bottom - margin
        }
    )
}

//private fun List<VerticalEdges>.filterInCenterProximityExclusivelyVerticallyFluctuatingOnes(image: Bitmap): List<VerticalEdges> {
//    val widthQueryOffsetPercentage = 0.4f
//    val horizontalQueryOffset: Int = (image.width * widthQueryOffsetPercentage).toInt()
//
//    return filter{ verticalEdges ->
//        (horizontalQueryOffset..image.width-horizontalQueryOffset)
//            .all{
//                image.hasFluctuationThroughoutColumn(it, verticalEdges.top, verticalEdges.height())
//            }
//        }
//}
//
//private fun Bitmap.hasFluctuationThroughoutColumn(x: Int, y: Int, candidateHeight: Int): Boolean{
//    val nPixelComparisonsPerColumn = 4
//    val step: Int = (candidateHeight + y) / nPixelComparisonsPerColumn
//
//    return (y + step until candidateHeight + y step step).any {
//        getPixel(x, it) != getPixel(x, it - step)
//    }
//}