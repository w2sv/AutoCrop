package com.autocrop.dataclasses

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class IOSynopsisTest {
    @ParameterizedTest
    @CsvSource(
        "54, 64, some:hyyyper SickIdentifier BroBro",
        "82, 0, dir",
        "0, 0, dir",
        "127, 127, dir",
    )
    fun encodingAndDecoding(nSavedCrops: Int, nDeletedScreenshots: Int, dirIdentifier: String) {
        val decodedIOSynopsis = IOSynopsis.fromByteArray(
            IOSynopsis(nSavedCrops, nDeletedScreenshots, dirIdentifier)
                .toByteArray()
        )

        Assertions.assertEquals(nSavedCrops, decodedIOSynopsis.nSavedCrops)
        Assertions.assertEquals(nDeletedScreenshots, decodedIOSynopsis.nDeletedScreenshots)
        Assertions.assertEquals(dirIdentifier, decodedIOSynopsis.cropWriteDirIdentifier)
    }
}