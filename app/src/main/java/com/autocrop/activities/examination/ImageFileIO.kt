package com.autocrop.activities.examination

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
import com.autocrop.picturesDir
import com.autocrop.utils.android.apiNotNewerThanQ
import com.autocrop.utils.android.debuggingModeEnabled
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun saveCropAndDeleteScreenshotIfApplicable(
    screenshotUri: Uri,
    crop: Bitmap,
    deleteScreenshot: Boolean,
    context: Context) {

    crop.save(context, screenshotUri.cropFileName)

    if (deleteScreenshot)
        screenshotUri.deleteUnderlyingImageFile(context)
}

/**
 * References:
 *      https://stackoverflow.com/a/10124040
 *      https://stackoverflow.com/a/59536115
 */
private fun Bitmap.save(context: Context, fileName: String) {
    val (newUri, fileOutputStream) = if (apiNotNewerThanQ)
        imageFileUriToOutputStreamUntilQ(fileName)
    else
        imageFileUriToOutputStreamPostQ(fileName, context)

    // write image
    compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
    fileOutputStream.close()

    newUri.notifyGalleryAboutFileModification(context)
    Timber.i("Saved crop to ${newUri.path}")
}

private fun imageFileUriToOutputStreamUntilQ(fileName: String): Pair<Uri, OutputStream> =
    File(picturesDir, fileName).let { destinationFile ->
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

private val Uri.cropFileName: String
    get() = fileName
        .replace("screenshot","AutoCrop",true)

private val Uri.fileName: String
    get() = File(path!!).name

/**
 * References:
 *      https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
 *      https://developer.android.com/training/data-storage/use-cases#modify-delete-media
 *      https://stackoverflow.com/a/12478822/12083276
 */
private fun Uri.deleteUnderlyingImageFile(context: Context) {
    val file = File(imageFilePath(context))

    // delete file and update media gallery
    with(context) {
        contentResolver.delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Images.ImageColumns.DATA + "=?",
            arrayOf(file.canonicalPath)
        )
        notifyGalleryAboutFileModification(this)
        MediaScannerConnection.scanFile(
            this,
            arrayOf(file.toString()),
            arrayOf("image/*")
        ) { _, _ -> }
    }

    // log deletion success if debugging
    if (debuggingModeEnabled()) {
        with(file.canonicalFile.absolutePath) {
            if (file.exists())
                Timber.e("Deletion of $this failed")
            else
                Timber.i("Successfully deleted $this")
        }
    }
}

private fun Uri.notifyGalleryAboutFileModification(context: Context) =
    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, this))

private fun Uri.imageFilePath(context: Context): String =
    context.contentResolver.query(
        this,
        arrayOf(MediaStore.Images.Media.DATA),
        null,
        null,
        null
    )!!.run {
        moveToFirst()
        getString(getColumnIndexOrThrow(MediaStore.Images.Media.DATA))!!
    }