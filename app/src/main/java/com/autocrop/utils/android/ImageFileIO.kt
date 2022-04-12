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
fun ContentResolver.saveBitmap(bitmap: Bitmap, fileName: String): Boolean = bitmap.compressToStream(
    when {
        apiNotNewerThanQ -> GetOutputStream.untilQ(fileName)
        else -> GetOutputStream.postQ(fileName, this)
    }
)
    .also {
        Timber.i(
            if (it) "Successfully wrote $fileName" else "Couldn't write $fileName"
        )
    }

fun ContentResolver.saveBitmap(bitmap: Bitmap, fileName: String, parentDocumentUri: Uri): Boolean =
    bitmap.compressToStream(GetOutputStream.fromParentDocument(fileName, parentDocumentUri, this))
        .also { Timber.i(
            if (it) "Successfully wrote $fileName to parentDocumentUri" else "Couldn't write $fileName to parentDocumentUri"
        )
    }

fun Bitmap.compressToStream(stream: OutputStream) =
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
        .also {stream.close()}

private object GetOutputStream{
    fun fromParentDocument(fileName: String, parentDocumentUri: Uri, contentResolver: ContentResolver): OutputStream =
        DocumentsContract.createDocument(contentResolver, parentDocumentUri, MimeTypes.JPEG, fileName)!!.run {
            contentResolver.openOutputStream(this)!!
        }

    fun untilQ(fileName: String): OutputStream =
        File(externalPicturesDir, fileName).run {
            FileOutputStream(this)
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun postQ(fileName: String, contentResolver: ContentResolver): OutputStream =
        contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.MIME_TYPE, MimeTypes.JPEG)
            }
        )!!.let { newUri ->
            contentResolver.openOutputStream(newUri)!!
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
fun ContentResolver.deleteImageMediaFile(uri: Uri): Boolean =
    (
            delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "${FILE_PATH_COLUMN_NAME}=?",
                arrayOf(imageFilePath(uri))
            ) != 0
    )
        .also{ Timber.i(if (it) "Successfully deleted screenshot" else "Couldn't delete screenshot") }

/**
 * @see
 *      https://stackoverflow.com/a/16511111/12083276
 *
 * Alternative solution: https://stackoverflow.com/a/38568666/12083276
 */
private fun ContentResolver.imageFilePath(uri: Uri): String =
    query(uri, arrayOf(FILE_PATH_COLUMN_NAME), null, null, null)!!.run {
        moveToFirst()
        getString(getColumnIndexOrThrow(FILE_PATH_COLUMN_NAME))!!
            .also { close() }
    }
