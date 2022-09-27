package com.autocrop.activities.examination

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.autocrop.dataclasses.CropBundle
import com.autocrop.utils.android.ImageMimeType
import com.autocrop.utils.android.extensions.compressToStream
import com.autocrop.utils.android.extensions.deleteImage
import com.autocrop.utils.android.extensions.queryMediaColumn
import com.autocrop.utils.android.externalPicturesDir
import com.autocrop.utils.kotlin.dateTimeNow
import com.autocrop.utils.kotlin.logBeforehand
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * first := deletionSuccessful
 * second := [mediaUriWithAppendedId] whose deletion confirmation has to be inquired
 *
 * first == true -> second=null
 * first == false -> second?
 * second =! null -> first = false
 */
typealias DeletionResult = Pair<Boolean, Uri?>

/**
 * first := savingSuccessful
 * second := writeUri
 */
typealias SavingResult = Pair<Boolean, Uri>

/**
 * Saves [CropBundle.crop] under [cropFileName] depending on [CropBundle.screenshot].uri
 * Deletes/triggers deletion of respective screenshot depending on [deleteScreenshot]
 */
fun Context.processCropBundle(
    cropBundle: CropBundle,
    validSaveDirDocumentUri: Uri?,
    deleteScreenshot: Boolean): Pair<SavingResult, DeletionResult?>{

    val cropSavingResult = contentResolver.saveBitmap(
        cropBundle.crop.bitmap,
        cropBundle.screenshot.parsedMimeType,
        cropFileName(cropBundle.screenshot.fileName, cropBundle.screenshot.parsedMimeType),
        validSaveDirDocumentUri
    )
    val screenshotDeletionResult = if (deleteScreenshot)
        attemptImageFileDeletion(cropBundle.screenshot.uri)
    else
        null

    return cropSavingResult to screenshotDeletionResult
}

fun cropFileName(fileName: String, mimeType: ImageMimeType): String =
    "${fileNameWOExtension(fileName)}-AutoCropped_${dateTimeNow()}.${mimeType.fileExtension}"

fun fileNameWOExtension(fileName: String): String =
    fileName.replaceAfterLast(".", "")
        .run { slice(0 until lastIndex) }

//$$$$$$$$$$$$$$
// Crop Saving $
//$$$$$$$$$$$$$$

/**
 * @see
 *      https://stackoverflow.com/a/10124040
 *      https://stackoverflow.com/a/59536115
 */
fun ContentResolver.saveBitmap(bitmap: Bitmap,
                               mimeType: ImageMimeType,
                               fileName: String,
                               parentDocumentUri: Uri? = null): SavingResult {
    val (outputStream, writeUri) = GetOutputStream(this, fileName, parentDocumentUri, mimeType)

    return bitmap.compressToStream(outputStream, mimeType.compressFormat)
        .also {
            Timber.i(if (it) "Successfully wrote $fileName" else "Couldn't write $fileName")
        } to writeUri
}

@SuppressLint("Recycle")  // Suppress 'OutputStream should be closed' warning
private object GetOutputStream{
    operator fun invoke(contentResolver: ContentResolver, fileName: String, parentDocumentUri: Uri?, mimeType: ImageMimeType): Pair<OutputStream, Uri> =
        when {
            parentDocumentUri != null -> fromParentDocument(fileName, contentResolver, parentDocumentUri, mimeType)
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q -> untilQ(fileName)
            else -> @RequiresApi(Build.VERSION_CODES.Q) {
                postQ(fileName, contentResolver, mimeType)
            }
        }

    private fun fromParentDocument(fileName: String,
                                   contentResolver: ContentResolver,
                                   parentDocumentUri: Uri,
                                   mimeType: ImageMimeType): Pair<OutputStream, Uri> = logBeforehand("GetOutputStream.fromParentDocument") {
        DocumentsContract.createDocument(
            contentResolver,
            parentDocumentUri,
            mimeType.string,
            fileName
        )!!.run {
            contentResolver.openOutputStream(this)!! to this
        }
    }

    private fun untilQ(fileName: String): Pair<OutputStream, Uri> = logBeforehand("GetOutputStream.untilQ") {
        File(externalPicturesDir, fileName).run {
            FileOutputStream(this) to Uri.fromFile(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun postQ(fileName: String, contentResolver: ContentResolver, mimeType: ImageMimeType): Pair<OutputStream, Uri> = logBeforehand("GetOutputStream.postQ") {
        contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType.string)
            }
        )!!.let { newUri ->
            contentResolver.openOutputStream(newUri)!! to newUri
        }
    }
}

//$$$$$$$$$$$$$$$$$$$$$$
// Screenshot Deletion $
//$$$$$$$$$$$$$$$$$$$$$$

/**
 * Starting from [Build.VERSION_CODES.R] deletion of external files requires explicit user permission,
 * therefore in that case merely returns retrieved [mediaUriWithAppendedId] for querying deletion confirmation
 * later on. Otherwise deletes file immediately
 */
private fun Context.attemptImageFileDeletion(uri: Uri): DeletionResult = logBeforehand("attemptImageFileDeletion for $uri") {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        false to uri.mediaUriWithAppendedId(this)
            .also { Timber.i("Returned mediaUriWithAppendedId: $it") }
    else
        contentResolver.deleteImage(uri) to null
            .also { Timber.i("Triggered immediate image file deletion") }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun Uri.mediaUriWithAppendedId(context: Context): Uri =
    ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        context.mediaUriId(this)
    )

@RequiresApi(Build.VERSION_CODES.Q)
private fun Context.mediaUriId(uri: Uri): Long =
    contentResolver
        .queryMediaColumn(uri, MediaStore.Images.Media._ID)
        .toLong()