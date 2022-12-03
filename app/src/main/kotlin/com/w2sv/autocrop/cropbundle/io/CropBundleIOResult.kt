package com.w2sv.autocrop.cropbundle.io

import android.net.Uri

data class CropBundleIOResult(val cropWriteUri: Uri?, var deletedScreenshot: Boolean?) {

    val successfullySavedCrop: Boolean
        get() =
            cropWriteUri != null
}