//package com.w2sv.cropbundle
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import com.w2sv.cropbundle.cropping.CropEdges
//import com.w2sv.cropbundle.cropping.crop
//import com.w2sv.cropbundle.utils.assetFileStream
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.params.ParameterizedTest
//import org.junit.jupiter.params.provider.Arguments
//import org.junit.jupiter.params.provider.MethodSource
//import org.opencv.android.OpenCVLoader
//import slimber.log.d
//import timber.log.Timber
//import java.io.File
//import java.util.stream.Stream
//
///**
// * Actually running unit tests which however can't be implemented as such, due to
// * inherent infeasibility of loading images
// */
//class CroppingTest {
//
//    companion object {
//        @BeforeAll
//        @JvmStatic
//        fun initialize() {
//            Timber.plant(Timber.DebugTree())
//            OpenCVLoader.initDebug()
//        }
//
//        @JvmStatic
//        fun validScreenshotCropEdges(): Stream<Arguments> =
//            Stream.of(
//                Arguments.of(
//                    "Screenshot_20220728-162238744.png",
//                    CropEdges(top = 274, bottom = 1200)
//                ),
//                Arguments.of(
//                    "Screenshot_20220826-162550339.png",
//                    CropEdges(top = 173, bottom = 713)
//                ),
//                Arguments.of(
//                    "Screenshot_20220908-192611095.png",
//                    CropEdges(top = 517, bottom = 962)
//                ),
//                Arguments.of(
//                    "Screenshot_20220930-053707818.png",
//                    CropEdges(top = 734, bottom = 1222)
//                ),
//                Arguments.of(
//                    "Screenshot_20220930-053803445.png",
//                    CropEdges(top = 323, bottom = 1222)
//                ),
//                Arguments.of(
//                    "Screenshot_20220930-054702883.png",
//                    CropEdges(top = 346, bottom = 1245)
//                ),
//                Arguments.of(
//                    "Screenshot_20220930-055714735.png",
//                    CropEdges(top = 389, bottom = 1287)
//                ),
//                Arguments.of(
//                    "Screenshot_20220930-055723303.png",
//                    CropEdges(top = 354, bottom = 1251)
//                ),
//                Arguments.of(
//                    "Screenshot_20220930-233304941.png",
//                    CropEdges(top = 499, bottom = 1218)
//                ),
//                Arguments.of(
//                    "Screenshot_20221001-004641375.png",
//                    CropEdges(top = 446, bottom = 1146)
//                ),
//                Arguments.of(
//                    "Screenshot_20221002-053122401.png",
//                    CropEdges(top = 309, bottom = 1197)
//                ),
//                Arguments.of(
//                    "Screenshot_20221003-220428354.png",
//                    CropEdges(top = 342, bottom = 1241)
//                ),
//                Arguments.of(
//                    "Screenshot_20221003-220437926.png",
//                    CropEdges(top = 350, bottom = 1249)
//                ),
//                Arguments.of(
//                    "Screenshot_20221003-220442543.png",
//                    CropEdges(top = 350, bottom = 1249)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-062231179.png",
//                    CropEdges(top = 391, bottom = 1290)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-062237943.png",
//                    CropEdges(top = 391, bottom = 1290)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-062325166.png",
//                    CropEdges(top = 476, bottom = 1195)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-094117652.png",
//                    CropEdges(top = 161, bottom = 1439)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-132418023.png",
//                    CropEdges(top = 385, bottom = 1275)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-230716108.png",
//                    CropEdges(top = 278, bottom = 1177)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-230758085.png",
//                    CropEdges(top = 265, bottom = 1163)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-230806963.png",
//                    CropEdges(top = 367, bottom = 1265)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-230814337.png",
//                    CropEdges(top = 367, bottom = 1265)
//                ),
//                Arguments.of(
//                    "Screenshot_20221004-231930304.png",
//                    CropEdges(top = 556, bottom = 1251)
//                ),
//                Arguments.of(
//                    "Screenshot_20221006-034942642.png",
//                    CropEdges(top = 326, bottom = 1225)
//                )
//            )
//
//        @JvmStatic
//        fun defectiveScreenshotCropEdges(): Stream<Arguments> =
//            Stream.of(
//                Arguments.of(
//                    "Screenshot_2022-12-06-09-26-12-769_com.android.chrome.jpg",
//                    CropEdges(top = 493, bottom = 1845)
//                ),
//                Arguments.of(
//                    "Screenshot_2022-12-05-19-33-16-693_com.android.chrome.jpg",
//                    CropEdges(top = 764, bottom = 1436)
//                )
//            )
//    }
//
//    @ParameterizedTest
//    @MethodSource
//    fun validScreenshotCropEdges(fileName: String, expected: CropEdges) {
//        assertEquals(
//            expected,
//            loadTestScreenshot(fileName, "valid").crop(150.toDouble())?.first
//        )
//    }
//
//    @ParameterizedTest
//    @MethodSource
//    fun defectiveScreenshotCropEdges(fileName: String, expected: CropEdges) {
//        d { "------------------------\nCroppingKt: $fileName" }
//        val delta = 1f
//        val (edges, _) = loadTestScreenshot(fileName, "defective").crop(150.toDouble())!!
//
//        assertEquals(expected.top.toFloat(), edges.top.toFloat(), delta)
//        assertEquals(expected.bottom.toFloat(), edges.bottom.toFloat(), delta)
//    }
//
//    //    @ParameterizedTest
//    //    @ValueSource(
//    //        strings = [
//    //            //            "Bari-Italy.jpg.1584966891939.image.750.563.low.jpg",  TODO
//    //            //            "IMG_20200709_175312.jpg",
//    //            "Screenshot_2021-04-13-22-48-46-461_com.android.chrome.png",
//    //            "Screenshot_2021-04-14-22-22-17-499_com.android.chrome.png",
//    //            "Screenshot_2021-04-18-21-58-06-781_com.android.chrome.png"
//    //        ]
//    //    )
//    //    fun invalidScreenshotsReturnNull(fileName: String) {
//    //        assertNull(
//    //            loadTestScreenshot(
//    //                fileName,
//    //                "invalid"
//    //            )
//    //                .crop()
//    //        )
//    //    }
//}
//
//private fun loadTestScreenshot(imageFileName: String, subDirName: String): Bitmap =
//    BitmapFactory.decodeStream(
//        assetFileStream(File(subDirName, imageFileName).path)
//    )