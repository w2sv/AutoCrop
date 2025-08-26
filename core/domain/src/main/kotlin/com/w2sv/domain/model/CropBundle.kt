package com.w2sv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropBundle(
    val screenshot: Screenshot,
    var crop: Crop, // TODO: vars
    var edgeCandidates: List<Int>,
    @CropSensitivity var cropSensitivity: Int
) : Parcelable {

    sealed interface CreationResult {
        @JvmInline
        value class Success(val cropBundle: CropBundle) : CreationResult
        data object NoCropEdgesFound : CreationResult
        data object BitmapLoadingFailed : CreationResult
    }
}
