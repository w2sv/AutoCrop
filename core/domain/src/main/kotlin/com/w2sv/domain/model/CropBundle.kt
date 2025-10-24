package com.w2sv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropBundle(
    val screenshot: Screenshot,
    val crop: Crop,
    val edgeCandidates: List<Int>,
    @CropSensitivity val cropSensitivity: Int
) : Parcelable {

    val id: String get() = screenshot.mediaStoreData.id.toString()

    sealed interface CreationResult {
        @JvmInline
        value class Success(val cropBundle: CropBundle) : CreationResult
        data object NoCropEdgesFound : CreationResult
        data object BitmapLoadingFailed : CreationResult
    }
}
