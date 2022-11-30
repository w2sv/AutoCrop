package com.w2sv.autocrop.activities.cropexamination

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CropBundleIOKtTest {

    @ParameterizedTest
    @CsvSource(
        "sadfa/myvcxlj/0/dir/file.jpg, /dir/file.jpg"
    )
    fun pathTail(input: String, expected: String) {
        assertEquals(expected, com.w2sv.autocrop.cropbundle.io.pathTail(input))
    }
}