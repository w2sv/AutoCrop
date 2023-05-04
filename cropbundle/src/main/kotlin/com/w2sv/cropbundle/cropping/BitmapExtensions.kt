package com.w2sv.cropbundle.cropping

import android.graphics.Bitmap

fun Bitmap.cropped(edges: CropEdges): Bitmap =
    Bitmap.createBitmap(
        this,
        0,
        edges.top + 1,
        width,
        edges.height - 1
    )

typealias CropResult = Pair<CropEdges, List<Int>>

fun Bitmap.crop(threshold: Double): CropResult? {
    val matRGBA = getMat()
    return getEdgeCandidates(matRGBA, threshold)?.let {
        getMaxScoreCropEdges(it, matRGBA) to it
    }
}