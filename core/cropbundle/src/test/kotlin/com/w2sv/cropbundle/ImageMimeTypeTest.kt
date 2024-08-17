package com.w2sv.cropbundle

import com.w2sv.cropbundle.io.ImageMimeType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ImageMimeTypeTest {
    @ParameterizedTest
    @CsvSource(
        "image/jpg, JPG",
        "image/jpeg, JPG",
        "image/png, PNG",
        "image/webp, WEBP",
        "image/jhsadfa, JPG",
    )
    fun parse(mediaStoreIdentifier: String, expectedEnumName: String) {
        assertEquals(expectedEnumName, ImageMimeType.parse(mediaStoreIdentifier).name)
    }
}