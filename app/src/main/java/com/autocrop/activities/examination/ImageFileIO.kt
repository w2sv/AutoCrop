package com.autocrop.activities.examination

import android.content.ContentResolver
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.types.CropBundle
import com.autocrop.utils.android.deleteImageMediaFile
import com.autocrop.utils.android.fileName
import com.autocrop.utils.android.saveBitmap

fun ContentResolver.processCropBundle(
    cropBundle: CropBundle,
    deleteScreenshot: Boolean,
    documentUriValid: Boolean?): Pair<Boolean, Boolean>{

    val successfullySavedCrop = if (documentUriValid == true)
        saveBitmap(
            cropBundle.crop,
            cropFileName(cropBundle.screenshotUri.fileName),
            CropFileSaveDestinationPreferences.documentUri!!
        )
    else
        saveBitmap(
            cropBundle.crop,
            cropFileName(cropBundle.screenshotUri.fileName)
        )

    return successfullySavedCrop to if (deleteScreenshot)
        deleteImageMediaFile(cropBundle.screenshotUri)
    else false
}

private fun cropFileName(fileName: String): String = fileName
    .replace("screenshot","AutoCrop",true)
    .run {
        if (contains("AutoCrop"))
            this
        else
            "AutoCrop_$this"
    }