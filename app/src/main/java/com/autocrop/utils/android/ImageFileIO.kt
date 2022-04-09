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
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object MimeTypes{
    const val IMAGE = "image/*"
    const val JPEG = "image/jpeg"
}

val externalPicturesDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

//$$$$$$$$$
// Saving $
//$$$$$$$$$

/**
 * @see
 *      https://stackoverflow.com/a/10124040
 *      https://stackoverflow.com/a/59536115
 */
fun Bitmap.save(contentResolver: ContentResolver, fileName: String, parentDocumentUri: Uri? = null): Uri? {
    val (fileOutputStream, writeUri) = when {
        parentDocumentUri != null -> OutputStreamWithUri.fromParentDocument(fileName, parentDocumentUri, contentResolver)
        apiNotNewerThanQ -> OutputStreamWithUri.untilQ(fileName)
        else -> OutputStreamWithUri.postQ(fileName, contentResolver)
    }

    // write image
    val successfullyCompressed: Boolean = compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
    fileOutputStream.close()

    Timber.i(if (successfullyCompressed) "Successfully saved bitmap $fileName" else "Couldn't save bitmap $fileName")
    return if (successfullyCompressed) writeUri else null
}

private object OutputStreamWithUri{
    fun fromParentDocument(fileName: String, parentDocumentUri: Uri, contentResolver: ContentResolver): Pair<OutputStream, Uri> =
        DocumentsContract.createDocument(contentResolver, parentDocumentUri, MimeTypes.JPEG, fileName)!!.run {
            contentResolver.openOutputStream(this)!! to this
        }

    fun untilQ(fileName: String): Pair<OutputStream, Uri> =
        File(externalPicturesDir, fileName).run {
            FileOutputStream(this) to Uri.fromFile(this)
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun postQ(fileName: String, contentResolver: ContentResolver): Pair<OutputStream, Uri> =
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

//$$$$$$$$$$$
// Deletion $
//$$$$$$$$$$$

const val FILE_PATH_COLUMN_NAME = MediaStore.Images.Media.DATA

/**
 * @see
 *      https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
 *      https://developer.android.com/training/data-storage/use-cases#modify-delete-media
 *      https://stackoverflow.com/a/12478822/12083276
 *
 * @return flag indicating whether image was successfully deleted
 */
fun Uri.deleteUnderlyingImageFile(contentResolver: ContentResolver): Boolean =
    contentResolver.delete(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        "${FILE_PATH_COLUMN_NAME}=?",
        arrayOf(imageFilePath(contentResolver))
    ) != 0
        .also{ Timber.i(if (it == 0) "Couldn't delete screenshot" else "Successfully deleted screenshot") }

/**
 * @see
 *      https://stackoverflow.com/a/16511111/12083276
 *
 * Alternative solution: https://stackoverflow.com/a/38568666/12083276
 */
private fun Uri.imageFilePath(contentResolver: ContentResolver): String =
    contentResolver.query(this, arrayOf(FILE_PATH_COLUMN_NAME),null,null,null)!!.run {
        moveToFirst()
        getString(getColumnIndexOrThrow(FILE_PATH_COLUMN_NAME))!!
            .also { close() }
    }
