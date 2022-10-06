package com.autocrop.activities.cropping

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.autocrop.activities.cropping.cropping.cropEdgesCandidates
import com.autocrop.activities.cropping.cropping.maxHeightEdges
import com.lyrebirdstudio.croppylib.CropEdges
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import utils.streamAssetFile
import java.io.File
import java.util.stream.Stream

/**
 * Actually running unit tests which however can't be implemented as such, due to
 * inherent infeasibility of loading images
 * "Screenshot_2021-02-20-20-44-37-742_com.android.chrome.png, 898, 38",
"Screenshot_2021-02-20-20-44-47-768_com.android.chrome.png, 898, 38",
"Screenshot_2021-02-20-20-45-01-118_com.android.chrome.png, 898, 38",
"Screenshot_2021-02-20-21-35-48-770_com.android.chrome.png, 898, 38",
"Screenshot_2021-02-20-21-36-25-277_com.android.chrome.png, 814, 44",
"Screenshot_2021-02-20-21-40-51-320_com.reddit.frontpage.png, 897, 38",
"Screenshot_2021-02-20-23-54-23-480_com.android.chrome.png, 897, 38",
"Screenshot_2021-02-20-23-54-37-101_com.android.chrome.png, 1104, 23",
"Screenshot_2021-02-20-23-54-46-890_com.android.chrome.png, 931, 35",
"Screenshot_2021-02-20-23-54-58-389_com.android.chrome.png, 992, 31"
 */
class CroppingTest {
    @ParameterizedTest
    @MethodSource
    fun validScreenshotCropEdges(fileName: String, expectedCropEdges: CropEdges) {
        Assertions.assertEquals(
            expectedCropEdges,
            loadTestScreenshot(fileName,"valid-screenshots").cropEdgesCandidates()!!.maxHeightEdges()
        )
    }

    companion object{
        @JvmStatic
        fun validScreenshotCropEdges(): Stream<Arguments> =
            Stream.of(
                Arguments.arguments("Screenshot_2021-02-20-20-44-37-742_com.android.chrome.png", CropEdges(23, 699))
            )
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "Bari-Italy.jpg.1584966891939.image.750.563.low.jpg",
        "E3zRSPPVUAIISnq.jpeg",
        "E4a_LCMXoAIr7vo.jpeg",
        "E33A8iMVcAQcd9n.jpeg",
        "E33LbepX0AE2RbR.jpeg",
        "IMG_20200709_175312.jpg",
        "Screenshot_2021-04-13-22-48-12-482_com.android.chrome.png",
        "Screenshot_2021-04-13-22-48-18-486_com.android.chrome.png",
        "Screenshot_2021-04-13-22-48-46-461_com.android.chrome.png",
        "Screenshot_2021-04-14-18-01-45-017_com.whatsapp.png"
    ])
    fun invalidScreenshotsReturnNull(fileName: String){
        Assertions.assertNull(
            loadTestScreenshot(
                fileName,
                "invalid-screenshots"
            )
                .cropEdgesCandidates()
        )
    }

    private fun loadTestScreenshot(imageFileName: String, subDirName: String): Bitmap =
        BitmapFactory.decodeStream(
            streamAssetFile(File(subDirName, imageFileName).path)
        )
}