package com.autocrop.activities.examination

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.autocrop.dataclasses.CropBundle
import com.autocrop.utils.kotlin.logBeforehand
import com.autocrop.utils.android.*
import timber.log.Timber

/**
 * first := deletionSuccessful
 * second := [mediaUriWithAppendedId] whose deletion has to be confirmed
 *
 * first=true -> second=null
 * first=false -> second=null | !null
 * second=!null -> first = false
 */
typealias DeletionResult = Pair<Boolean, Uri?>

/**
 * Saves [CropBundle.crop] under [cropFileName] depending on [CropBundle.screenshot].uri
 * Deletes/triggers deletion of respective screenshot depending on [deleteScreenshot]
 */
fun Context.processCropBundle(
    cropBundle: CropBundle,
    validSaveDirDocumentUri: Uri?,
    deleteScreenshot: Boolean): Pair<SavingResult, DeletionResult?>{

    println(cropBundle.screenshot)

    val cropSavingResult = contentResolver.saveBitmap(
        cropBundle.crop.bitmap,
        cropFileName(cropBundle.screenshot.uri.fileName()),
        validSaveDirDocumentUri
    )
    val screenshotDeletionResult = if (deleteScreenshot)
        attemptImageFileDeletion(cropBundle.screenshot.uri)
    else
        null

    return cropSavingResult to screenshotDeletionResult
}

//$$$$$$$$$$$$$$
// Crop Saving $
//$$$$$$$$$$$$$$

fun cropFileName(fileName: String): String =
    fileName.split(".").run {
        "${first()}_AutoCropped.${last()}"
    }

//$$$$$$$$$$$$$$$$$$$$$$
// Screenshot Deletion $
//$$$$$$$$$$$$$$$$$$$$$$

/**
 * Starting from [Build.VERSION_CODES.R] deletion of external files requires explicit user permission,
 * therefore in that case merely returns retrieved [mediaUriWithAppendedId] for querying deletion confirmation
 * later on
 *
 * Otherwise deletes file immediately
 */
private fun Context.attemptImageFileDeletion(uri: Uri): DeletionResult = logBeforehand("attemptImageFileDeletion for $uri") {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        false to uri.mediaUriWithAppendedId(this)
            .also { Timber.i("Returned mediaUriWithAppendedId") }
    else
        contentResolver.deleteImageMediaFile(uri) to null
            .also { Timber.i("Triggered immediate image file deletion") }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun Uri.mediaUriWithAppendedId(context: Context): Uri =
    ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        context.mediaUriId(this)
    )
        .also { Timber.i("Constructed mediaUriWithAppendedId: $it") }

@RequiresApi(Build.VERSION_CODES.Q)
private fun Context.mediaUriId(uri: Uri): Long =
    contentResolver.queryImageFileMediaColumn(uri, MediaStore.Images.Media._ID).toLong()