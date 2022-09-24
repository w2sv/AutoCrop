package com.autocrop.activities.examination

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ImageFileIOKtTest {

    @ParameterizedTest
    @CsvSource(
        "screenshot234.png, AutoCrop234.png",
        "234.png, AutoCrop_234.png"
    )
    fun cropFileNameEquality(fileName: String, expected: String) {
        Assertions.assertEquals(expected, cropFileName(fileName))
    }
}