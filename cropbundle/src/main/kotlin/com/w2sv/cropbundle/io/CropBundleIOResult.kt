package com.w2sv.cropbundle.io

import android.net.Uri

data class CropBundleIOResult(val cropFileUri: Uri?, var screenshotDeletionResult: ScreenshotDeletionResult) {

    val successfullySavedCrop: Boolean
        get() =
            cropFileUri != null
}