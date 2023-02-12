package com.w2sv.cropbundle.cropping

import com.w2sv.androidutils.utils.measured
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import slimber.log.d

internal fun getEdgeCandidates(matRGBA: Mat, threshold: Double): List<Int>? {
    val matGrayScale = Mat()
    Imgproc.cvtColor(matRGBA, matGrayScale, Imgproc.COLOR_RGBA2GRAY)

    val matCanny = Mat()
    Imgproc.Canny(matGrayScale, matCanny, 100.0, 200.0)

    return measured(methodLabel = "getCandidates") {
        (0 until matCanny.rows()).filter { i ->
            matCanny.row(i).singleChannelMean() > threshold
        }
            .run {
                if (isEmpty())
                    null
                else
                    listOf(0) + this + listOf(matCanny.rows())
            }
    }
}

internal fun getMaxScoreCropEdges(candidates: List<Int>, matRGBA: Mat): CropEdges {
    d { "Candidates: $candidates" }

    val matSobel = Mat()
    measured(methodLabel = "Sobel Computation") {
        Imgproc.Sobel(matRGBA, matSobel, CvType.CV_16U, 2, 2, 5)
    }

    var maxScore = 0f
    var maxScoreEdges: CropEdges? = null

    measured(methodLabel = "MaxScoreCropEdges Computation") {
        candidates.windowed(2)
            .map { CropEdges(it) }
            .forEach { edges ->
                val cropAreaMean: Float = matSobel.getCrop(edges).multiChannelMean().toFloat()
                val heightPortion: Float = edges.height.toFloat() / matSobel.rows().toFloat()
                val score: Float = cropAreaMean * heightPortion

                if (score > maxScore) {
                    maxScore = score
                    maxScoreEdges = edges
                }
            }
    }

    return maxScoreEdges!!
}