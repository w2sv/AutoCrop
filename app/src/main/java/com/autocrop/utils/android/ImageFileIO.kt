package com.autocrop.utils.android

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
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
fun Bitmap.save(context: Context, fileName: String) {
    val (newUri, fileOutputStream) = if (apiNotNewerThanQ)
        imageFileUriToOutputStreamUntilQ(fileName)
    else
        imageFileUriToOutputStreamPostQ(fileName, context)

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
private fun imageFileUriToOutputStreamPostQ(fileName: String, context: Context): Pair<Uri, OutputStream> =
    context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
    )!!.let { newUri ->
        newUri to context.contentResolver.openOutputStream(newUri)!!
    }

/**
 * References:
 *      https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
 *      https://developer.android.com/training/data-storage/use-cases#modify-delete-media
 *      https://stackoverflow.com/a/12478822/12083276
 */
fun Uri.deleteUnderlyingImageFile(context: Context) {
    val file = imageFile(context)

    // delete file and update media gallery
    with(context) {
        contentResolver.delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
             "${MediaStore.Images.ImageColumns._ID}=?",
            arrayOf(file.canonicalPath)
        )
//        notifyGalleryAboutFileModification(this)
        MediaScannerConnection.scanFile(
            this,
            arrayOf(file.toString()),
            arrayOf(IMAGE_MIME_TYPE)
        ) { _, _ -> }
    }
}

private fun Uri.notifyGalleryAboutFileModification(context: Context) =
    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, this))

/**
 * Reference: https://stackoverflow.com/a/16511111/12083276
 *
 * Alternative solution: https://stackoverflow.com/a/38568666/12083276
 */
private fun Uri.imageFile(context: Context): File =
    context.contentResolver.query(
        this,
        arrayOf(MediaStore.Images.Media.DATA),
        null,
        null,
        null
    )!!.run {
        moveToFirst()
        File(getString(getColumnIndexOrThrow(MediaStore.Images.Media.DATA))!!)
    }