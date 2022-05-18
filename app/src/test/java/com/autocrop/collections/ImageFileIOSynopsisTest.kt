package com.autocrop.collections

import org.junit.Assert
import org.junit.jupiter.api.Test

internal class ImageFileIOSynopsisTest{
    @Test
    fun encodingAndDecoding(){
        val nSavedCrops = 54
        val nDeletedScreenshots = 64
        val cropWriteDirIdentifier = "some:hyyyper SickIdentifier BroBro"

        val decodedImageFileIOSynopsis = ImageFileIOSynopsis(
            ImageFileIOSynopsis(nSavedCrops, nDeletedScreenshots, cropWriteDirIdentifier)
                .toByteArray()
        )

        Assert.assertEquals(nSavedCrops, decodedImageFileIOSynopsis.nSavedCrops)
        Assert.assertEquals(nDeletedScreenshots, decodedImageFileIOSynopsis.nDeletedScreenshots)
        Assert.assertEquals(cropWriteDirIdentifier, decodedImageFileIOSynopsis.cropWriteDirIdentifier)
    }
}