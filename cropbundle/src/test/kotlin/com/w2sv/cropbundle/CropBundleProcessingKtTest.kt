package com.w2sv.cropbundle

import com.w2sv.cropbundle.io.ImageMimeType
import com.w2sv.cropbundle.io.cropFileName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CropBundleProcessingKtTest {
    @ParameterizedTest
    @CsvSource(
        "screenshot234.png, screenshot234-AutoCropped_[0-9]{8}_[0-9]{6}.png",
        "234.png, 234-AutoCropped_[0-9]{8}_[0-9]{6}.png",
    )
    fun testCropFileName(fileName: String, match_regex: String) {
        Assertions.assertTrue(
            Regex(match_regex)
                .matches(
                    cropFileName(
                        fileName,
                        ImageMimeType.PNG
                    )
                )
        )
    }
}