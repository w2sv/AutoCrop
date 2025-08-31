package com.w2sv.domain.model

import android.net.Uri

data class CropResult(val cropFileUri: Uri?, val screenshotDeletionResult: Screenshot.DeletionResult?) {

    val successfullySavedCrop: Boolean
        get() = cropFileUri != null
}
