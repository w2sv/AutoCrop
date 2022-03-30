package com.autocrop.utils.android

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

const val IMAGE_MIME_TYPE = "image/*"

/**
 * References:
 *      https://stackoverflow.com/a/10124040
 *      https://stackoverflow.com/a/59536115
 */
fun Bitmap.save(contentResolver: ContentResolver, fileName: String) {
    val (newUri, fileOutputStream) = if (apiNotNewerThanQ)
        imageFileUriToOutputStreamUntilQ(fileName)
    else
        imageFileUriToOutputStreamPostQ(fileName, contentResolver)

    // write image
    compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
    fileOutputStream.close()

    // newUri.notifyGalleryAboutFileModification(context)
    Timber.i("Saved crop to ${newUri.path}")
}

private fun imageFileUriToOutputStreamUntilQ(fileName: String): Pair<Uri, OutputStream> =
    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName).let { destinationFile ->
        Uri.fromFile(destinationFile) to FileOutputStream(destinationFile)
    }

@RequiresApi(Build.VERSION_CODES.Q)
private fun imageFileUriToOutputStreamPostQ(fileName: String, contentResolver: ContentResolver): Pair<Uri, OutputStream> =
    contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
    )!!.let { newUri ->
        newUri to contentResolver.openOutputStream(newUri)!!
    }

const val FILE_PATH_COLUMN_NAME = MediaStore.Images.Media.DATA

/**
 * References:
 *      https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
 *      https://developer.android.com/training/data-storage/use-cases#modify-delete-media
 *      https://stackoverflow.com/a/12478822/12083276
 */
fun Uri.deleteUnderlyingImageFile(contentResolver: ContentResolver): Boolean = contentResolver.delete(
    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    "${FILE_PATH_COLUMN_NAME}=?",
    arrayOf(imageFilePath(contentResolver))
) != 0.also{ Timber.i(if (it == 0) "Couldn't delete screenshot" else "Deleted screenshot") }

/**
 * Reference: https://stackoverflow.com/a/16511111/12083276
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
