package com.autocrop.activities.examination

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.utils.android.deleteUnderlyingImageFile
import com.autocrop.utils.android.fileName
import com.autocrop.utils.android.save

fun saveCropAndDeleteScreenshotIfApplicable(
    screenshotUri: Uri,
    crop: Bitmap,
    deleteScreenshot: Boolean,
    contentResolver: ContentResolver): Uri? {

    val writeUri = crop.save(contentResolver, cropFileName(screenshotUri.fileName), CropFileSaveDestinationPreferences.documentUri)

    if (deleteScreenshot)
        screenshotUri.deleteUnderlyingImageFile(contentResolver)

    return writeUri
}

private fun cropFileName(fileName: String): String = fileName
    .replace("screenshot","AutoCrop",true)
    .run {
        if (contains("AutoCrop"))
            this
        else
            "AutoCrop_$this"
    }