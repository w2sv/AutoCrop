package com.autocrop.collections

import org.junit.Assert
import org.junit.jupiter.api.Test

internal class CropIOResultsTest{
    @Test
    fun encodingAndDecoding(){
        val nSavedCrops = 54
        val nDeletedScreenshots = 64
        val cropWriteDirIdentifier = "some:hyyyper SickIdentifier BroBro"

        val decodedCropIOResults = CropIOResults(
            CropIOResults(nSavedCrops, nDeletedScreenshots, cropWriteDirIdentifier)
                .toByteArray()
        )

        Assert.assertEquals(nSavedCrops, decodedCropIOResults.nSavedCrops)
        Assert.assertEquals(nDeletedScreenshots, decodedCropIOResults.nDeletedScreenshots)
        Assert.assertEquals(cropWriteDirIdentifier, decodedCropIOResults.cropWriteDirIdentifier)
    }
}