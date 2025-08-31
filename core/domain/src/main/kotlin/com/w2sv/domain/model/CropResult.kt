package com.w2sv.domain.model

import android.net.Uri

data class CropResult(val cropFileUri: Uri?, val screenshotDeletionResult: ScreenshotDeletionResult?) {

    val successfullySavedCrop: Boolean
        get() = cropFileUri != null
}
