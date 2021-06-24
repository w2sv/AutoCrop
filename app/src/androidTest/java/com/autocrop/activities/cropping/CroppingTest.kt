package com.autocrop.activities.cropping

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.filters.MediumTest
import com.autocrop.utils.android.picturesDir
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(JUnitParamsRunner::class)
@MediumTest
class CroppingTest {

    companion object{
        val TEST_RESOURCES_DIR: File = File(picturesDir, "TestResources")
    }

    /**
     * Method enabling retrieval of crop metrics thereupon to be asserted
     *
     * Uncomment @After decorator, paste in file names whose crop metrics are to be found,
     * copy paste retrieved parameter rows into respective @Parameters
     */
    // @After
    fun displayCropMetrics(){
            """Screenshot_2021-02-20-20-44-27-697_com.android.chrome.png
               Screenshot_2021-02-20-20-44-37-742_com.android.chrome.png
               Screenshot_2021-02-20-20-44-47-768_com.android.chrome.png
               Screenshot_2021-02-20-20-45-01-118_com.android.chrome.png
               Screenshot_2021-02-20-21-35-48-770_com.android.chrome.png
               Screenshot_2021-02-20-21-36-25-277_com.android.chrome.png
               Screenshot_2021-02-20-21-40-51-320_com.reddit.frontpage.png
               Screenshot_2021-02-20-23-54-23-480_com.android.chrome.png
               Screenshot_2021-02-20-23-54-37-101_com.android.chrome.png
               Screenshot_2021-02-20-23-54-46-890_com.android.chrome.png
               Screenshot_2021-02-20-23-54-58-389_com.android.chrome.png"""
            .split("\n")
            .map { s -> s.filter { !it.isWhitespace() } }
            .forEach {
                with(
                    croppedImage(
                        loadTestScreenshot(it)
                    )!!
                ){
                    println("$it, ${first.width}, ${first.height}, $second")
                }
            }
    }

    @Test
    fun paths(){
        assert(picturesDir.exists())
        assert(TEST_RESOURCES_DIR.exists())
    }

    @Test
    @Parameters(value = [
        "Screenshot_2021-02-20-20-44-37-742_com.android.chrome.png, 720, 897, 62",
        "Screenshot_2021-02-20-20-44-47-768_com.android.chrome.png, 720, 897, 62",
        "Screenshot_2021-02-20-20-45-01-118_com.android.chrome.png, 720, 897, 62",
        "Screenshot_2021-02-20-21-35-48-770_com.android.chrome.png, 720, 897, 62",
        "Screenshot_2021-02-20-21-36-25-277_com.android.chrome.png, 720, 813, 56",
        "Screenshot_2021-02-20-21-40-51-320_com.reddit.frontpage.png, 720, 896, 62",
        "Screenshot_2021-02-20-23-54-23-480_com.android.chrome.png, 720, 896, 62",
        "Screenshot_2021-02-20-23-54-37-101_com.android.chrome.png, 720, 1103, 77",
        "Screenshot_2021-02-20-23-54-46-890_com.android.chrome.png, 720, 930, 65",
        "Screenshot_2021-02-20-23-54-58-389_com.android.chrome.png, 720, 991, 69"
    ])
    fun croppedImageValidScreenshots(screenshotFileName: String, cropWidth: Int, cropHeight: Int, retentionPercentage: Int) {
        with(croppedImage(loadTestScreenshot(screenshotFileName))!!){
            assertEquals(cropWidth, first.width)
            assertEquals(cropHeight, first.height)

            assertEquals(retentionPercentage, second)
        }
    }

//    @Test
//    @Parameters(value = [])
//    fun croppedImageInvalidScreenshots(screenshotsFileName: String){
//        assertEquals(null, croppedImage(loadTestScreenshot(screenshotsFileName)))
//    }

    private fun loadTestScreenshot(imageFileName: String): Bitmap =
        BitmapFactory.decodeFile(File(TEST_RESOURCES_DIR, imageFileName).absolutePath)
}