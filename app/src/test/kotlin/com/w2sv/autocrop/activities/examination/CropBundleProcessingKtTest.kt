package com.w2sv.autocrop.activities.examination

import com.w2sv.autocrop.cropbundle.io.ImageMimeType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CropBundleProcessingKtTest {
    @ParameterizedTest
    @CsvSource(
        "screenshot234.png, screenshot234-AutoCropped_[0-9]{8}_[0-9]{6}.png",
        "234.png, 234-AutoCropped_[0-9]{8}_[0-9]{6}.png",
    )
    fun cropFileName(fileName: String, match_regex: String) {
        Assertions.assertTrue(
            Regex(match_regex)
                .matches(
                    com.w2sv.autocrop.cropbundle.io.cropFileName(
                        fileName,
                        ImageMimeType.PNG
                    )
                )
        )
    }
}