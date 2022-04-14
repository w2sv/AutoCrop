package com.autocrop.activities.examination

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.types.CropBundle
import com.autocrop.utils.android.deleteImageMediaFile
import com.autocrop.utils.android.fileName
import com.autocrop.utils.android.queryImageFileMediaColumn
import com.autocrop.utils.android.saveBitmap
import com.autocrop.utils.logBeforehand

typealias DeletionResult = Pair<Boolean, Uri?>

fun Context.processCropBundle(
    cropBundle: CropBundle,
    documentUriValid: Boolean?,
    deleteScreenshot: Boolean): Pair<Boolean, DeletionResult?>{

    val cropSuccessfullySaved = contentResolver.saveCrop(
        cropBundle.crop,
        cropFileName(cropBundle.screenshotUri.fileName),
        if (documentUriValid == true) CropFileSaveDestinationPreferences.documentUri else null
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

private fun ContentResolver.saveCrop(crop: Bitmap, cropFileName: String, saveDirDocumentUri: Uri?): Boolean =
    saveDirDocumentUri?.let {
        saveBitmap(
            crop,
            cropFileName,
            saveDirDocumentUri
        )
    } ?: saveBitmap(
        crop,
        cropFileName
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

private fun Context.attemptImageFileDeletion(uri: Uri): DeletionResult = logBeforehand("attemptImageFileDeletion for $uri") {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        false to uri.mediaUriWithAppendedId(this)
    contentResolver.deleteImageMediaFile(uri) to null
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun Uri.mediaUriWithAppendedId(context: Context) =
    ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        context.mediaUriId(this)
    )

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.mediaUriId(uri: Uri): Long =
    contentResolver.queryImageFileMediaColumn(uri, MediaStore.Images.Media._ID)
        .toLong()
