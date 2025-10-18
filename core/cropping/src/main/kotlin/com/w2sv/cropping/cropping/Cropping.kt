package com.w2sv.cropping.cropping

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import com.w2sv.cropping.io.extensions.loadBitmap
import com.w2sv.cropping.io.queryMediaStoreData
import com.w2sv.domain.model.Crop
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.model.CropEdges
import com.w2sv.domain.model.CropParameters
import com.w2sv.domain.model.CropSensitivity
import com.w2sv.domain.model.Screenshot
import kotlin.math.roundToInt
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import slimber.log.d
import slimber.log.e

fun Bitmap.cropParameters(@CropSensitivity sensitivity: Int): CropParameters? {
    val matRGBA = getMat()
    return getEdgeCandidates(matRGBA, sensitivity)?.let {
        CropParameters(
            edges = getMaxScoreCropEdges(candidates = it, matRGBA = matRGBA),
            candidates = it
        )
    }
}

fun Bitmap.crop(screenshotDiskUsage: Long, edges: CropEdges): Crop {
    val cropBitmap = cropped(edges)
    val discardedPercentageF = (height - cropBitmap.height).toFloat() / height.toFloat()

    return Crop(
        bitmap = cropBitmap,
        edges = edges,
        discardedPercentage = (discardedPercentageF * 100).roundToInt(),
        discardedKB = (discardedPercentageF * screenshotDiskUsage / 1000).roundToInt().toLong()
    )
}

fun createCropBundle(
    screenshotMediaUri: Uri,
    @CropSensitivity cropSensitivity: Int,
    contentResolver: ContentResolver
): CropBundle.CreationResult =
    try {
        when (val screenshotBitmap = contentResolver.loadBitmap(screenshotMediaUri)) {
            null -> CropBundle.CreationResult.BitmapLoadingFailed
            else -> {
                when (val cropResult = screenshotBitmap.cropParameters(cropSensitivity)) {
                    null -> CropBundle.CreationResult.NoCropEdgesFound
                    else -> {
                        val screenshot = Screenshot(
                            uri = screenshotMediaUri,
                            height = screenshotBitmap.height,
                            mediaStoreData = queryMediaStoreData(
                                contentResolver,
                                screenshotMediaUri
                            )
                        )
                        CropBundle.CreationResult.Success(
                            cropBundle = CropBundle(
                                screenshot = screenshot,
                                crop = screenshotBitmap.crop(
                                    screenshotDiskUsage = screenshot.mediaStoreData.diskUsage,
                                    edges = cropResult.edges
                                ),
                                edgeCandidates = cropResult.candidates,
                                cropSensitivity = cropSensitivity
                            )
                        )
                    }
                }
            }
        }
    } catch (e: Exception) {
        e(e)
        CropBundle.CreationResult.BitmapLoadingFailed // TODO
    }

private fun Bitmap.cropped(edges: CropEdges): Bitmap =
    Bitmap.createBitmap(
        this,
        0,
        edges.top + 1,
        width,
        edges.height - 1
    )

private fun getEdgeCandidates(matRGBA: Mat, @CropSensitivity sensitivity: Int): List<Int>? {
    // Convert to gray scale
    val matGrayScale = Mat()
    Imgproc.cvtColor(matRGBA, matGrayScale, Imgproc.COLOR_RGBA2GRAY)

    // Get canny edge detected matrix
    val matCanny = Mat()
    Imgproc.Canny(matGrayScale, matCanny, 100.0, 200.0)

    // Convert sensitivity to threshold
    val threshold = edgeCandidateThreshold(sensitivity)

    return (0 until matCanny.rows()).filter { i ->
        matCanny.row(i).singleChannelMean() > threshold
    }
        .run {
            if (isEmpty()) {
                null
            } else {
                listOf(0) + this + listOf(matCanny.rows())
            }
        }
}

private fun getMaxScoreCropEdges(candidates: List<Int>, matRGBA: Mat): CropEdges {
    d { "Candidates: $candidates" }

    val matSobel = Mat()
    Imgproc.Sobel(matRGBA, matSobel, CvType.CV_16U, 2, 2, 5)

    var maxScore = 0f
    var maxScoreEdges: CropEdges? = null

    candidates
        .windowed(2)
        .map { (top, bottom) -> CropEdges(top, bottom) }
        .forEach { edges ->
            val cropAreaMean: Float = matSobel.getCrop(edges).multiChannelMean().toFloat()
            val heightPortion: Float = edges.height.toFloat() / matSobel.rows().toFloat()
            val score: Float = cropAreaMean * heightPortion

            if (score > maxScore) {
                maxScore = score
                maxScoreEdges = edges
            }
        }

    return maxScoreEdges!!
}
