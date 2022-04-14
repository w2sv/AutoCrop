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
import com.autocrop.utils.android.queryImageFileMediaColumn
import com.autocrop.utils.android.saveBitmap
import timber.log.Timber

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
    Timber.i("attemptImageFileDeletion called for $uri")
    val isDocumentUri: Boolean = DocumentsContract.isDocumentUri(this, uri)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        return false to uri.mediaUriWithAppendedId(this, isDocumentUri)
    return deleteImageFile(uri, false) to null
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun Uri.mediaUriWithAppendedId(context: Context, isDocumentUri: Boolean) =
    ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        context.mediaUriId(this, isDocumentUri)
    )

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.mediaUriId(uri: Uri, isDocumentUri: Boolean): Long =
    (
            if (isDocumentUri)
                MediaStore.getMediaUri(this, uri)!!.lastPathSegment!!
            else
                contentResolver.queryImageFileMediaColumn(uri, MediaStore.Images.Media._ID)
    )
        .toLong()

// TODO: add custom MaxApi=<R annotation
private fun Context.deleteImageFile(uri: Uri, isDocumentUri: Boolean): Boolean =
    if (isDocumentUri)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q)
            contentResolver.deleteImageMediaFile(MediaStore.getMediaUri(this, uri)!!)
        else
            DocumentsContract.deleteDocument(contentResolver, uri)  // TODO: fix for API=29: java.lang.SecurityException: Permission Denial: writing com.android.externalstorage.ExternalStorageProvider uri content://com.android.externalstorage.documents/document/171D-0F06%3APictures%2FScreenshot_2021-02-25-06-27-25-296_com.android.chrome.png from pid=697, uid=10150 requires android.permission.MANAGE_DOCUMENTS, or grantUriPermission()
                .also { Timber.i("Deleting screenshot via DocumentsContract.deleteDocument") }
    else
        contentResolver.deleteImageMediaFile(uri)
            .also { Timber.i("Deleting screenshot via deleteImageMediaFile") }
