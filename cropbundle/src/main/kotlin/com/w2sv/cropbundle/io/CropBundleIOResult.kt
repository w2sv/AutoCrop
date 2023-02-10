package com.w2sv.cropbundle.io

import android.net.Uri

data class CropBundleIOResult(val cropUri: Uri?, var deletedScreenshot: Boolean?) {

    val successfullySavedCrop: Boolean
        get() =
            cropUri != null
}