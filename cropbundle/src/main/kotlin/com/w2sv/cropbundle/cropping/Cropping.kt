package com.w2sv.cropbundle.cropping

import android.graphics.Bitmap

fun Bitmap.cropped(edges: CropEdges): Bitmap =
    Bitmap.createBitmap(
        this,
        0,
        edges.top,
        width,
        edges.height
    )

fun Bitmap.cropEdges(): CropEdges? =
    cropEdgesCandidates()
        ?.maxHeightEdges()

fun Bitmap.cropEdgesCandidates(): List<CropEdges>? =
    rawCropEdgesCandidates()
        //        .verticalFluctuationComprisingEdges(this)
        .run {
            ifEmpty { null }
        }

fun List<CropEdges>.maxHeightEdges(): CropEdges =
    maxByOrNull { it.height }!!.run {
        val excludeMargin = 1  // TODO Uhm...
        CropEdges(top + excludeMargin to bottom - excludeMargin)
    }

//private fun List<CropEdges>.verticalFluctuationComprisingEdges(
//    image: Bitmap,
//    bilateralWidthOffsetPercentage: Float = 0.4f,
//    pixelComparisonsBetweenCropEdges: Int = 4
//): List<CropEdges> {
//    val horizontalOffsetPixels: Int by lazy {
//        (image.width * bilateralWidthOffsetPercentage).toInt()
//    }
//
//    return filter { edges ->
//        val columnTraversalStep = edges.height / pixelComparisonsBetweenCropEdges  // TODO
//        columnTraversalStep < 1 || (horizontalOffsetPixels..image.width - horizontalOffsetPixels)
//            .all { x ->
//                image.cropEdgesDelimitedColumnNotMonochromatic(x, edges, columnTraversalStep)
//            }
//    }
//}
//
//private fun Bitmap.cropEdgesDelimitedColumnNotMonochromatic(x: Int, edges: CropEdges, step: Int): Boolean =
//    (edges.top + step..edges.bottom step step)
//        .any { y ->
//            getPixel(x, y - step) != getPixel(x, y)
//        }
