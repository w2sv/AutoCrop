package com.autocrop.dataclasses

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class IOSynopsisTest {
    @Test
    fun encodingAndDecoding() {
        val nSavedCrops = 54
        val nDeletedScreenshots = 64
        val cropWriteDirIdentifier = "some:hyyyper SickIdentifier BroBro"

        val decodedIOSynopsis = IOSynopsis(
                IOSynopsis(nSavedCrops, nDeletedScreenshots, cropWriteDirIdentifier)
                        .toByteArray()
        )

        Assertions.assertEquals(nSavedCrops, decodedIOSynopsis.nSavedCrops)
        Assertions.assertEquals(nDeletedScreenshots, decodedIOSynopsis.nDeletedScreenshots)
        Assertions.assertEquals(cropWriteDirIdentifier, decodedIOSynopsis.cropWriteDirIdentifier)
    }
}