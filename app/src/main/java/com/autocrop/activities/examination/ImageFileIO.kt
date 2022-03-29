package com.autocrop.activities.examination

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.autocrop.utils.android.deleteUnderlyingImageFile
import com.autocrop.utils.android.fileName
import com.autocrop.utils.android.save

fun saveCropAndDeleteScreenshotIfApplicable(
    screenshotUri: Uri,
    crop: Bitmap,
    deleteScreenshot: Boolean,
    context: Context) {

    crop.save(context, screenshotUri.autoCropFileName)

    if (deleteScreenshot)
        screenshotUri.deleteUnderlyingImageFile(context)
}

private val Uri.autoCropFileName: String
    get() = fileName
        .replace("screenshot","AutoCrop",true)