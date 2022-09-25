package com.autocrop.activities.examination

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ImageFileIOKtTest {

    @ParameterizedTest
    @CsvSource(
        "screenshot234.png, screenshot234_AutoCropped.png",
        "234.png, 234_AutoCropped.png",
        "234.jpg, 234_AutoCropped.jpg",
    )
    fun cropFileNameEquality(fileName: String, expected: String) {
        Assertions.assertEquals(expected, cropFileName(fileName))
    }
}