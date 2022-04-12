package com.autocrop.activities.examination

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.types.CropBundle
import com.autocrop.utils.android.deleteImageMediaFile
import com.autocrop.utils.android.fileName
import com.autocrop.utils.android.saveBitmap

typealias DeletionResult = Pair<Uri?, Boolean?>

fun Context.processCropBundle(
    cropBundle: CropBundle,
    deleteScreenshot: Boolean,
    documentUriValid: Boolean?): Pair<Boolean, DeletionResult?>{

    val cropSuccessfullySaved = contentResolver.saveCrop(cropBundle.crop, cropFileName(cropBundle.screenshotUri.fileName), documentUriValid)
    val screenshotDeletionResult = if (deleteScreenshot)
        deleteImageFile(cropBundle.screenshotUri)
    else
        null

    return cropSuccessfullySaved to screenshotDeletionResult
}

private fun ContentResolver.saveCrop(crop: Bitmap, cropFileName: String, documentUriValid: Boolean?): Boolean =
    if (documentUriValid == true)
        saveBitmap(
            crop,
            cropFileName,
            CropFileSaveDestinationPreferences.documentUri!!
        )
    else
        saveBitmap(
            crop,
            cropFileName(cropFileName)
        )

private fun cropFileName(fileName: String): String = fileName
    .replace("screenshot","AutoCrop",true)
    .run {
        if (contains("AutoCrop"))
            this
        else
            "AutoCrop_$this"
    }

private fun Context.deleteImageFile(uri: Uri): DeletionResult {
    var deletionUri: Uri? = null
    var successfullyDeleted: Boolean? = null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        deletionUri = uri.mediaUriWithAppendedId(this)
    else
        successfullyDeleted = if (DocumentsContract.isDocumentUri(this, uri))
            DocumentsContract.deleteDocument(contentResolver, uri)
        else
            contentResolver.deleteImageMediaFile(uri)

    return deletionUri to successfullyDeleted
}

@RequiresApi(Build.VERSION_CODES.R)
private fun Uri.mediaUriWithAppendedId(context: Context) = ContentUris.withAppendedId(
    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    (if (DocumentsContract.isDocumentUri(context, this)) MediaStore.getMediaUri(context, this)!! else this)
        .lastPathSegment!!
        .toLong()
)