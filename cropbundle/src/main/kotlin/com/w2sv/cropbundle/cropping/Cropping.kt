package com.w2sv.cropbundle.cropping

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import slimber.log.d

internal fun getEdgeCandidates(matRGBA: Mat): List<Int>? {
    val matGrayScale = Mat()
    Imgproc.cvtColor(matRGBA, matGrayScale, Imgproc.COLOR_RGBA2GRAY)

    val matCanny = Mat()
    Imgproc.Canny(matGrayScale, matCanny, 100.0, 200.0)

    //    matCanny.logInfo("Canny")

    return measured("getCandidates") {
        getCandidates(matCanny, 150.0).ifEmpty { null }
    }
}

@Suppress("SameParameterValue")
private fun getCandidates(matCanny: Mat, threshold: Double): List<Int> {
    return (0 until matCanny.rows()).filter { i ->
        matCanny.row(i).singleChannelMean() > threshold
    }
}

internal fun getMaxScoreCropEdges(matRGBA: Mat, incompleteCandidates: List<Int>): CropEdges {
    d { "Candidates: $incompleteCandidates" }

    val candidates = listOf(0) + incompleteCandidates + listOf(matRGBA.rows())

    val matSobel = Mat()
    measured("Sobel Computation") {
        Imgproc.Sobel(matRGBA, matSobel, CvType.CV_16U, 2, 2, 5)
    }

    //    matSobel.logInfo("Sobel")

    var maxScore = 0f
    var maxScoreEdges: CropEdges? = null

    measured("MaxScoreCropEdges Computation") {
        candidates.windowed(2)
            .map { CropEdges(it) }
            .forEach { edges ->
                val cropAreaMean: Float = matSobel.cropArea(edges).multiChannelMean().toFloat()
//                    .also { d { "cropAreaMean=$it" } }
                val heightPortion: Float = edges.height.toFloat() / matSobel.rows().toFloat()
                val score: Float = cropAreaMean * heightPortion
//                    .also { d { "score=$it" } }

                if (score > maxScore) {
                    maxScore = score
                    maxScoreEdges = edges
                }
            }
    }

    return maxScoreEdges!!
}