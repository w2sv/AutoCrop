//package com.w2sv.autocrop.ui.model
//
//internal class CropSensitivityKtTest {
//
//    @ParameterizedTest
//    @CsvSource(
//        "0, 255",
//        "20, 50",
//        "4, 214",
//        "15, 101"
//    )
//    fun edgeCandidateThreshold(sensitivity: Int, expected: Int) {
//        Assertions.assertEquals(expected, com.w2sv.cropbundle.cropping.edgeCandidateThreshold(sensitivity))
//    }
//
//    @ParameterizedTest
//    @CsvSource(
//        "50, 20",
//        "255, 0",
//        "101, 15",
//        "214, 4"
//    )
//    fun cropSensitivity(threshold: Int, expected: Int) {
//        Assertions.assertEquals(expected, com.w2sv.cropbundle.cropping.cropSensitivity(threshold))
//    }
//}
