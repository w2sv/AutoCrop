package com.autocrop.utils.android

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
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

object MimeTypes{
    const val IMAGE = "image/*"
    const val JPEG = "image/jpeg"
}

val externalPicturesDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)  // TODO

//$$$$$$$$$
// Saving $
//$$$$$$$$$

/**
 * @see
 *      https://stackoverflow.com/a/10124040
 *      https://stackoverflow.com/a/59536115
 */
fun ContentResolver.saveBitmap(bitmap: Bitmap, fileName: String, parentDocumentUri: Uri? = null): Pair<Boolean, Uri>{
    val (outputStream, writeUri) = when {
        parentDocumentUri != null -> GetOutputStream.fromParentDocument(fileName, this, parentDocumentUri)
        apiNotNewerThanQ -> GetOutputStream.untilQ(fileName)
        else -> GetOutputStream.postQ(fileName, this)
    }

    return (bitmap.compressToStream(outputStream) to writeUri)
        .also {
            Timber.i(
                if (it.first) "Successfully wrote $fileName" else "Couldn't write $fileName"
            )
        }
}

fun Bitmap.compressToStream(stream: OutputStream) =
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
        .also {stream.close()}

private object GetOutputStream{
    fun fromParentDocument(fileName: String, contentResolver: ContentResolver, parentDocumentUri: Uri): Pair<OutputStream, Uri> = logBeforehand("GetOutputStream.fromParentDocument") {
        DocumentsContract.createDocument(
            contentResolver,
            parentDocumentUri,
            MimeTypes.JPEG,
            fileName
        )!!.run {
            contentResolver.openOutputStream(this)!! to this
        }
    }

    fun untilQ(fileName: String): Pair<OutputStream, Uri> = logBeforehand("GetOutputStream.untilQ") {
        File(externalPicturesDir, fileName).run {
            FileOutputStream(this) to Uri.fromFile(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun postQ(fileName: String, contentResolver: ContentResolver): Pair<OutputStream, Uri> = logBeforehand("GetOutputStream.postQ") {
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
    (
            delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "${MediaStore.Images.Media.DATA}=?",
                arrayOf(queryImageFileMediaColumn(uri, MediaStore.Images.Media.DATA))
            ) != 0
    )
        .also{ Timber.i(if (it) "Successfully deleted screenshot" else "Couldn't delete screenshot") }

/**
 * @see
 *      https://stackoverflow.com/a/16511111/12083276
 *
 * Alternative solution: https://stackoverflow.com/a/38568666/12083276
 */
fun ContentResolver.queryImageFileMediaColumn(uri: Uri, mediaColumn: String, selection: String? = null, selectionArgs: Array<String>? = null): String =
    query(uri, arrayOf(mediaColumn), selection, selectionArgs, null)!!.run {
        moveToFirst()
        getString(getColumnIndexOrThrow(mediaColumn))!!
            .also { close() }
    }
