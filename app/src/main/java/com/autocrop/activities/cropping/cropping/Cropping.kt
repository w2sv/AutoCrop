package com.autocrop.activities.cropping.cropping

import android.graphics.Bitmap

fun Bitmap.cropped(edges: CropEdges): Bitmap{
    val padding = 1
    return Bitmap.createBitmap(
        this,
        0,
        edges.top + padding,
        width,
        edges.height - padding
    )
}

fun Bitmap.cropEdgesCandidates(): List<CropEdges>? =
    rawCropEdgesCandidates()
        .verticalFluctuationComprisingEdges(this)
        .run {
            ifEmpty { null }
        }

fun List<CropEdges>.maxHeightEdges(): CropEdges =
    maxByOrNull {it.height}!!.run {
        val excludeMargin = 1
        CropEdges(top + excludeMargin to bottom - excludeMargin)
    }

private fun List<CropEdges>.verticalFluctuationComprisingEdges(image: Bitmap,
                                                               bilateralWidthOffsetPercentage: Float = 0.4f,
                                                               pixelComparisonsBetweenCropEdges: Int = 4): List<CropEdges> {
    val horizontalOffsetPixels: Int by lazy {
        (image.width * bilateralWidthOffsetPercentage).toInt()
    }

    return filter{ edges ->
        val columnTraversalStep = edges.height / pixelComparisonsBetweenCropEdges
        (horizontalOffsetPixels..image.width - horizontalOffsetPixels)
            .all{ x ->
                image.cropEdgesDelimitedColumnNotMonochromatic(x, edges, columnTraversalStep)
            }
        }
}

private fun Bitmap.cropEdgesDelimitedColumnNotMonochromatic(x: Int, edges: CropEdges, step: Int): Boolean =
    (edges.top + step..edges.bottom step step)
        .any {y ->
            getPixel(x, y - step) != getPixel(x, y)
        }
