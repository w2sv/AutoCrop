package com.w2sv.domain.model

import android.net.Uri

data class CropBundleProcessingResult(val cropFileUri: Uri?, val screenshotDeletionResult: Screenshot.DeletionResult?) {

    val successfullySavedCrop: Boolean
        get() = cropFileUri != null
}
