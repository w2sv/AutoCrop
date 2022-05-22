package com.autocrop.activities.examination

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import com.autocrop.collections.CropBundle
import com.autocrop.utils.logBeforehand
import com.autocrop.utilsandroid.deleteImageMediaFile
import com.autocrop.utilsandroid.fileName
import com.autocrop.utilsandroid.queryImageFileMediaColumn
import com.autocrop.utilsandroid.saveBitmap
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
 * first := savingSuccessful
 * second := writeUri
 */
typealias SavingResult = Pair<Boolean, Uri>

/**
 * Saves [CropBundle.crop] under [cropFileName] depending on [CropBundle.screenshotUri]
 * Deletes/triggers deletion of respective screenshot depending on [deleteScreenshot]
 */
fun Context.processCropBundle(
    cropBundle: CropBundle,
    validSaveDirDocumentUri: Uri?,
    deleteScreenshot: Boolean): Pair<SavingResult, DeletionResult?>{

    val cropSavingResult = contentResolver.saveBitmap(
        cropBundle.crop,
        cropFileName(cropBundle.screenshotUri.fileName),
        validSaveDirDocumentUri
    )
    val screenshotDeletionResult = if (deleteScreenshot)
        attemptImageFileDeletion(cropBundle.screenshotUri)
    else
        null

    return cropSavingResult to screenshotDeletionResult
}

//$$$$$$$$$$$$$$
// Crop Saving $
//$$$$$$$$$$$$$$

/**
 * Replaces 'screenshot' by 'AutoCrop' if present in [fileName], otherwise adds it as prefix
 */
fun cropFileName(fileName: String): String{
    val appName = "AutoCrop"

    return fileName
        .replace("screenshot", appName,true)
        .run {
            if (contains(appName))
                this
            else
                "${appName}_$this"
        }
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
        DocumentsContract.deleteDocument(contentResolver, uri) to null
//        DocumentFile.fromSingleUri(this, uri)!!.delete() to null
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
    MediaStore.getMediaUri(this, uri)!!.lastPathSegment!!.toLong()

//    contentResolver.queryImageFileMediaColumn(MediaStore.getMediaUri(this, uri)!!, MediaStore.Images.Media._ID).toLong()
