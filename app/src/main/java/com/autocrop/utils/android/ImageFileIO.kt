package com.autocrop.utils.android

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.autocrop.global.SaveDestinationPreferences
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

const val IMAGE_MIME_TYPE = "image/*"
private const val JPEG_MIME_TYPE = "image/jpeg"

/**
 * @see
 *      https://stackoverflow.com/a/10124040
 *      https://stackoverflow.com/a/59536115
 */
fun Bitmap.save(contentResolver: ContentResolver, fileName: String, pickedDestination: Uri? = null): Boolean {
    val fileOutputStream: OutputStream = when {
        SaveDestinationPreferences.documentUri != null -> imageFileUriToOutputStreamFromPickedPath(fileName, contentResolver)
        apiNotNewerThanQ -> imageFileUriToOutputStreamUntilQ(fileName)
        else -> imageFileUriToOutputStreamPostQ(fileName, contentResolver)
    }

    // write image
    val successfullyCompressed: Boolean = compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
    fileOutputStream.close()

    Timber.i("Successfully compressed: $successfullyCompressed")
    return successfullyCompressed
}

val externalPicturesDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

private fun imageFileUriToOutputStreamFromPickedPath(fileName: String, contentResolver: ContentResolver): OutputStream{
    println("treeUri: ${SaveDestinationPreferences.treeUri!!.path} ")
    println("authority: ${SaveDestinationPreferences.treeUri!!.authority}")
    println("documentUri: ${SaveDestinationPreferences.documentUri!!.path}")
    println("authority: ${SaveDestinationPreferences.documentUri!!.authority}")

    return DocumentsContract.createDocument(contentResolver, SaveDestinationPreferences.documentUri!!, JPEG_MIME_TYPE, fileName)!!.run {
        contentResolver.openOutputStream(this)!!
    }
}

private fun imageFileUriToOutputStreamUntilQ(fileName: String): OutputStream =
    FileOutputStream(File(externalPicturesDir, fileName))

@RequiresApi(Build.VERSION_CODES.Q)
private fun imageFileUriToOutputStreamPostQ(fileName: String, contentResolver: ContentResolver): OutputStream =
    contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.MIME_TYPE, JPEG_MIME_TYPE)
        }
    )!!.let { newUri ->
        contentResolver.openOutputStream(newUri)!!
    }

const val FILE_PATH_COLUMN_NAME = MediaStore.Images.Media.DATA

/**
 * @see
 *      https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
 *      https://developer.android.com/training/data-storage/use-cases#modify-delete-media
 *      https://stackoverflow.com/a/12478822/12083276
 *
 * @return flag indicating whether image was successfully deleted
 */
fun Uri.deleteUnderlyingImageFile(contentResolver: ContentResolver): Boolean = contentResolver.delete(
    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    "${FILE_PATH_COLUMN_NAME}=?",
    arrayOf(imageFilePath(contentResolver))
) != 0.also{ Timber.i(if (it == 0) "Couldn't delete screenshot" else "Deleted screenshot") }

/**
 * @see
 *      https://stackoverflow.com/a/16511111/12083276
 *
 * Alternative solution: https://stackoverflow.com/a/38568666/12083276
 */
private fun Uri.imageFilePath(contentResolver: ContentResolver): String{
    with(contentResolver.query(this, arrayOf(FILE_PATH_COLUMN_NAME),null,null,null)!!) {
        moveToFirst()
        return getString(getColumnIndexOrThrow(FILE_PATH_COLUMN_NAME))!!
            .also { close() }
    }
}

//fun Uri.deleteUnderlyingImageFile(context: Context){
////    val uriExternal: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//
//    println("deletionUri: $path")
//    with(context.contentResolver.query(this, arrayOf(MediaStore.Images.Media._ID), null, null, null)!!){
//        val columnIndexID = getColumnIndexOrThrow(MediaStore.Images.Media._ID)
//        moveToFirst()
//
//        val id = getLong(columnIndexID)
//        val contentUri = ContentUris.withAppendedId(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            id
//        )
//        println("id: $id")
//
//        val nDeletedRows = context.contentResolver.delete(
//            contentUri,
//            "${MediaStore.Images.Media._ID} = ?",
//            arrayOf(id.toString())
//        )
//        println("nDeletedRows: $nDeletedRows")
//        close()
//    }
//}

fun Uri.getPath(context: Context): String? {
    if (!DocumentsContract.isDocumentUri(context, this))
        throw IllegalArgumentException("${this.path} no DocumentUri")

    val docId = DocumentsContract.getDocumentId(this)

    if (DocumentsContract.isDocumentUri(context, this)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(this)) {
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true))
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]

            // TODO handle non-primary volumes
        } else if (isDownloadsDocument(this)) {
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(docId)
            )
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(this)) {
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(
                split[1]
            )
            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    }
    return null
}

fun getDataColumn(
    context: Context, uri: Uri?, selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
        column
    )
    try {
        cursor = context.getContentResolver().query(uri!!, projection, selection, selectionArgs,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val index: Int = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}
fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}
fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

