package com.autocrop.activities.cropping

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.autocrop.activities.cropping.fragments.cropping.cropRect
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import utils.streamAssetFile
import java.io.File

/**
 * Actually running unit tests which however can't be implemented as such due to
 * inherent infeasibility of loading images
 */
class CroppingTest {
    @ParameterizedTest
    @CsvSource(
        "Screenshot_2021-02-20-20-44-37-742_com.android.chrome.png, 720, 897, 38",
        "Screenshot_2021-02-20-20-44-47-768_com.android.chrome.png, 720, 897, 38",
        "Screenshot_2021-02-20-20-45-01-118_com.android.chrome.png, 720, 897, 38",
        "Screenshot_2021-02-20-21-35-48-770_com.android.chrome.png, 720, 897, 38",
        "Screenshot_2021-02-20-21-36-25-277_com.android.chrome.png, 720, 813, 44",
        "Screenshot_2021-02-20-21-40-51-320_com.reddit.frontpage.png, 720, 896, 38",
        "Screenshot_2021-02-20-23-54-23-480_com.android.chrome.png, 720, 896, 38",
        "Screenshot_2021-02-20-23-54-37-101_com.android.chrome.png, 720, 1103, 23",
        "Screenshot_2021-02-20-23-54-46-890_com.android.chrome.png, 720, 930, 35",
        "Screenshot_2021-02-20-23-54-58-389_com.android.chrome.png, 720, 991, 31"
    )
    fun validScreenshotCroppingResults(fileName: String, expectedWidth: Int, expectedHeight: Int, expectedRetentionPercentage: Int) {
        val (crop, retentionPercentage) = cropRect(
            loadTestScreenshot(
                fileName,
                "valid-screenshots"
            )
        )!!

        Assert.assertEquals(expectedWidth, crop.width)
        Assert.assertEquals(expectedHeight, crop.height)
        Assert.assertEquals(expectedRetentionPercentage, retentionPercentage)
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
        Assert.assertNull(
            cropRect(
                loadTestScreenshot(
                    fileName,
                    "invalid-screenshots"
                )
            )
        )
    }

    private fun loadTestScreenshot(imageFileName: String, subDirName: String): Bitmap =
        BitmapFactory.decodeStream(
            streamAssetFile(File(subDirName, imageFileName).path)
        )
}