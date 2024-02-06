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

fun Bitmap.crop(@CropSensitivity sensitivity: Int): CropResult? {
    val matRGBA = getMat()
    return getEdgeCandidates(matRGBA, sensitivity)?.let {
        CropResult(
            edges = getMaxScoreCropEdges(candidates = it, matRGBA = matRGBA),
            candidates = it
        )
    }
}