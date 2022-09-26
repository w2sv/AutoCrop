package com.autocrop.utils.android

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.autocrop.utils.kotlin.logBeforehand
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun ContentResolver.openBitmap(uri: Uri): Bitmap =
    BitmapFactory.decodeStream(openInputStream(uri))

@Suppress("DEPRECATION")
val externalPicturesDir: File =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

//$$$$$$$$$
// Saving $
//$$$$$$$$$

/**
 * first := savingSuccessful
 * second := writeUri
 */
typealias SavingResult = Pair<Boolean, Uri>

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

private fun Bitmap.compressToStream(stream: OutputStream, compressFormat: CompressFormat): Boolean =
    compress(compressFormat, 100, stream)
        .also {stream.close()}

@SuppressLint("Recycle")  // Suppress 'OutputStream should be closed' warning
private object GetOutputStream{
    operator fun invoke(contentResolver: ContentResolver, fileName: String, parentDocumentUri: Uri?, mimeType: ImageMimeType): Pair<OutputStream, Uri> =
        when {
            parentDocumentUri != null -> fromParentDocument(fileName, contentResolver, parentDocumentUri, mimeType)
            buildVersionNotNewerThanQ -> untilQ(fileName)
            else -> @RequiresApi(Build.VERSION_CODES.Q) {
                postQ(fileName, contentResolver)
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
    private fun postQ(fileName: String, contentResolver: ContentResolver): Pair<OutputStream, Uri> = logBeforehand("GetOutputStream.postQ") {
        contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.MIME_TYPE, ImageMimeType.JPEG.string)
            }
        )!!.let { newUri ->
            contentResolver.openOutputStream(newUri)!! to newUri
        }
    }
}

//$$$$$$$$$$$
// Deletion $
//$$$$$$$$$$$

/**
 * @see
 *      https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
 *      https://developer.android.com/training/data-storage/use-cases#modify-delete-media
 *      https://stackoverflow.com/a/12478822/12083276
 *
 * @return flag indicating whether image was successfully deleted
 */
fun ContentResolver.deleteImageMediaFile(uri: Uri): Boolean =
    try {
        (
            delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "${MediaStore.Images.Media._ID}=?",
                arrayOf(queryMediaColumn(uri, MediaStore.Images.Media._ID))
            ) != 0
        )
            .also{ Timber.i(if (it) "Successfully deleted screenshot" else "Couldn't delete screenshot") }
    } catch (e: NullPointerException){
        false
            .also { Timber.i(e) }
    }

/**
 * @see
 *      https://stackoverflow.com/a/16511111/12083276
 */
fun ContentResolver.queryMediaColumn(uri: Uri,
                                     mediaColumn: String,
                                     selection: String? = null,
                                     selectionArgs: Array<String>? = null): String =
    queryMediaColumns(uri, arrayOf(mediaColumn), selection, selectionArgs).first()

fun ContentResolver.queryMediaColumns(uri: Uri,
                                     mediaColumns: Array<String>,
                                     selection: String? = null,
                                     selectionArgs: Array<String>? = null): List<String> =
    query(
        uri,
        mediaColumns,
        selection,
        selectionArgs,
        null
    )!!.run {
        moveToFirst()
        mediaColumns.map { getString(getColumnIndexOrThrow(it)) }
            .also { close() }
    }