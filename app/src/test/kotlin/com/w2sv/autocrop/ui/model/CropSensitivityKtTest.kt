package com.w2sv.autocrop.ui.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CropSensitivityKtTest {

    @ParameterizedTest
    @CsvSource(
        "0, 255",
        "20, 50",
        "4, 214",
        "15, 101"
    )
    fun edgeCandidateThreshold(sensitivity: Int, expected: Int) {
        Assertions.assertEquals(expected, edgeCandidateThreshold(sensitivity))
    }

    @ParameterizedTest
    @CsvSource(
        "50, 20",
        "255, 0",
        "101, 15",
        "214, 4"
    )
    fun cropSensitivity(threshold: Int, expected: Int) {
        Assertions.assertEquals(expected, cropSensitivity(threshold))
    }
}