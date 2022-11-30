package com.w2sv.autocrop.cropbundle.io

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import com.w2sv.autocrop.cropbundle.Screenshot

/**
 * Saves [cropBitmap] under [cropFileName]
 * Deletes/triggers deletion of respective screenshot depending on [deleteScreenshot]
 */
fun ContentResolver.carryOutCropIO(
    cropBitmap: Bitmap,
    screenshotMediaStoreData: Screenshot.MediaStoreData,
    validSaveDirDocumentUri: Uri?,
    deleteScreenshot: Boolean
): IOResult =
    screenshotMediaStoreData.let {
        IOResult(
            saveBitmap(
                cropBitmap,
                it.parsedMimeType,
                cropFileName(it.fileName, it.parsedMimeType),
                validSaveDirDocumentUri
            ),
            deleteScreenshotIfApplicable(it.id, deleteScreenshot)
        )
    }

data class IOResult(val cropWriteUri: Uri?, var deletedScreenshot: Boolean?) {

    val successfullySavedCrop: Boolean
        get() =
            cropWriteUri != null
}