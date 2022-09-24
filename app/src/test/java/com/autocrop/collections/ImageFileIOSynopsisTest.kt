package com.autocrop.collections

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ImageFileIOSynopsisTest {
    @Test
    fun encodingAndDecoding() {
        val nSavedCrops = 54
        val nDeletedScreenshots = 64
        val cropWriteDirIdentifier = "some:hyyyper SickIdentifier BroBro"

        val decodedImageFileIOSynopsis = ImageFileIOSynopsis(
                ImageFileIOSynopsis(nSavedCrops, nDeletedScreenshots, cropWriteDirIdentifier)
                        .toByteArray()
        )

        Assertions.assertEquals(nSavedCrops, decodedImageFileIOSynopsis.nSavedCrops)
        Assertions.assertEquals(nDeletedScreenshots, decodedImageFileIOSynopsis.nDeletedScreenshots)
        Assertions.assertEquals(cropWriteDirIdentifier, decodedImageFileIOSynopsis.cropWriteDirIdentifier)
    }
}