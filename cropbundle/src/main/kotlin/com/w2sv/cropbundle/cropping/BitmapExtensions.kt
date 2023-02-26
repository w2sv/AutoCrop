package com.w2sv.cropbundle.cropping

import android.graphics.Bitmap
import com.w2sv.common.DEFAULT_EDGE_CANDIDATE_THRESHOLD

fun Bitmap.cropped(edges: CropEdges): Bitmap =
    Bitmap.createBitmap(
        this,
        0,
        edges.top + 1,
        width,
        edges.height - 1
    )

typealias CropResult = Pair<CropEdges, List<Int>>

fun Bitmap.crop(threshold: Double = DEFAULT_EDGE_CANDIDATE_THRESHOLD): CropResult? {
    val matRGBA = getMat()
    return getEdgeCandidates(matRGBA, threshold)?.let {
        getMaxScoreCropEdges(it, matRGBA) to it
    }
}