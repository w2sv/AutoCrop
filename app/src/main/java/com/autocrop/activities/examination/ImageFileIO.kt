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

typealias DeletionResult = Pair<Boolean, Uri?>

fun Context.processCropBundle(
    cropBundle: CropBundle,
    documentUriValid: Boolean?,
    deleteScreenshot: Boolean): Pair<Boolean, DeletionResult?>{

    val cropSuccessfullySaved = contentResolver.saveCrop(
        cropBundle.crop,
        cropFileName(cropBundle.screenshotUri.fileName),
        documentUriValid
    )
    val screenshotDeletionResult = if (deleteScreenshot)
        attemptImageFileDeletion(cropBundle.screenshotUri)
    else
        null

    return cropSuccessfullySaved to screenshotDeletionResult
}

//$$$$$$$$$$$$$$
// Crop Saving $
//$$$$$$$$$$$$$$

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

//$$$$$$$$$$$$$$$$$$$$$$
// Screenshot Deletion $
//$$$$$$$$$$$$$$$$$$$$$$

private fun Context.attemptImageFileDeletion(uri: Uri): DeletionResult {
    val isDocumentUri: Boolean = DocumentsContract.isDocumentUri(this, uri)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        return false to uri.mediaUriWithAppendedId(this, isDocumentUri)
    return contentResolver.deleteImageFile(uri, isDocumentUri) to null
}

@RequiresApi(Build.VERSION_CODES.R)
private fun Uri.mediaUriWithAppendedId(context: Context, isDocumentUri: Boolean) =
    ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        mediaUriId(if (isDocumentUri) MediaStore.getMediaUri(context, this)!! else this)
    )

fun mediaUriId(mediaUri: Uri): Long =
    mediaUri.lastPathSegment!!.toLong()

// TODO: add custom MaxApi=<R annotation
private fun ContentResolver.deleteImageFile(uri: Uri, isDocumentUri: Boolean): Boolean =
    if (isDocumentUri)
        DocumentsContract.deleteDocument(this, uri)
    else
        deleteImageMediaFile(uri)