package com.w2sv.cropbundle.cropping

import android.graphics.Bitmap
import com.w2sv.common.DEFAULT_CROP_EDGE_CANDIDATE_THRESHOLD

fun Bitmap.cropped(edges: CropEdges): Bitmap =
    Bitmap.createBitmap(
        this,
        0,
        edges.top + 1,
        width,
        edges.height - 1
    )

fun Bitmap.getCropEdges(threshold: Double = DEFAULT_CROP_EDGE_CANDIDATE_THRESHOLD): CropEdges? {
    val matRGBA = getMat()
    return getEdgeCandidates(matRGBA, threshold)?.let {
        getMaxScoreCropEdges(matRGBA, it)
    }
}