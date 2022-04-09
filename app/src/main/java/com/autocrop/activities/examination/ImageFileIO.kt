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

    val writeUri = crop.save(contentResolver, screenshotUri.cropFileName, CropFileSaveDestinationPreferences.documentUri)

    if (deleteScreenshot)
        screenshotUri.deleteUnderlyingImageFile(contentResolver)

    return writeUri
}

private val Uri.cropFileName: String
    get() = fileName
        .replace("screenshot","AutoCrop",true)  // TODO