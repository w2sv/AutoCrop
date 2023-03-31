package com.w2sv.cropbundle

import com.w2sv.cropbundle.io.utils.extensionLessFileName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class PathKtTest {

    //    @ParameterizedTest
    //    @CsvSource(
    //        "sadfa/myvcxlj/0/dir/file.jpg, /dir/file.jpg"
    //    )
    //    fun pathTail(input: String, expected: String) {
    //        assertEquals(expected, pathTail(input))
    //    }

    @ParameterizedTest
    @CsvSource(
        "screenshot234.png, screenshot234",
        "234.something.png, 234.something",
    )
    fun extensionLessFileName(fileName: String, expected: String) {
        assertEquals(expected, extensionLessFileName(fileName))
    }
}