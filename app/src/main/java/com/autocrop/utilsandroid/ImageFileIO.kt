package com.autocrop.utilsandroid

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.autocrop.utils.logBeforehand
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * In KB
 */
fun Bitmap.approximateJpegSize(): Int =
    allocationByteCount / 10 / 1024

fun ContentResolver.openBitmap(uri: Uri): Bitmap =
    BitmapFactory.decodeStream(openInputStream(uri))

object MimeTypes{
    const val IMAGE = "image/*"
    const val JPEG = "image/jpeg"
}

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
fun ContentResolver.saveBitmap(bitmap: Bitmap, fileName: String, parentDocumentUri: Uri? = null): SavingResult{
    val (outputStream, writeUri) = GetOutputStream(this, fileName, parentDocumentUri)

    return bitmap.compressToStream(outputStream)
        .also {
            Timber.i(
                if (it) "Successfully wrote $fileName" else "Couldn't write $fileName"
            )
        } to writeUri
}

fun Bitmap.compressToStream(stream: OutputStream): Boolean =
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
        .also {stream.close()}

private object GetOutputStream{
    operator fun invoke(contentResolver: ContentResolver, fileName: String, parentDocumentUri: Uri?): Pair<OutputStream, Uri> =
        when {
            parentDocumentUri != null -> fromParentDocument(fileName, contentResolver, parentDocumentUri)
            buildVersionNotNewerThanQ -> untilQ(fileName)
            else -> @RequiresApi(Build.VERSION_CODES.Q) {
                postQ(fileName, contentResolver)
            }
        }

    private fun fromParentDocument(fileName: String, contentResolver: ContentResolver, parentDocumentUri: Uri): Pair<OutputStream, Uri> = logBeforehand("GetOutputStream.fromParentDocument") {
        DocumentsContract.createDocument(
            contentResolver,
            parentDocumentUri,
            MimeTypes.JPEG,
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
                put(MediaStore.MediaColumns.MIME_TYPE, MimeTypes.JPEG)
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
                arrayOf(queryImageFileMediaColumn(uri, MediaStore.Images.Media._ID))
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
fun ContentResolver.queryImageFileMediaColumn(uri: Uri,
                                              mediaColumn: String,
                                              selection: String? = null,
                                              selectionArgs: Array<String>? = null): String =
    query(
        uri,
        arrayOf(mediaColumn),
        selection,
        selectionArgs,
        null
    )!!.run {
        moveToFirst()
        getString(getColumnIndexOrThrow(mediaColumn))!!
            .also { close() }
    }
