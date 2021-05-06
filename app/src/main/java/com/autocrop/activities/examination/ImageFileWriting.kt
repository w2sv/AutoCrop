package com.autocrop.activities.examination

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.autocrop.UserPreferences
import com.autocrop.utils.android.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


fun saveCropAndDeleteScreenshotIfApplicable(
    crop: Bitmap,
    screenshotUri: Uri,
    context: Context
) {
    crop.save(context, screenshotUri.imageFileName(context))

    // delete screenshot if applicable
    if (UserPreferences.deleteInputScreenshots)
        screenshotUri.deleteUnderlyingImageFile(context)
}


/**
 * References:
 *      https://stackoverflow.com/a/10124040
 *      https://stackoverflow.com/a/59536115
 */
private fun Bitmap.save(context: Context, title: String){

    // set file output stream and target file uri
    val (fileOutputStream: OutputStream, imageFileUri: Uri) = if (apiLowerEquals(29)) {
        File(
            Environment.getExternalStoragePublicDirectory(UserPreferences.relativeCropSaveDirPath)
                .toString(),
            title
        ).run {
            Pair(FileOutputStream(this), Uri.fromFile(this))
        }

    } else {
        val imageFileUri: Uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                this.put(MediaStore.MediaColumns.DISPLAY_NAME, title)
                this.put(MediaStore.MediaColumns.RELATIVE_PATH, UserPreferences.relativeCropSaveDirPath)
                this.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            }
        )!!

        Pair(context.contentResolver.openOutputStream(imageFileUri)!!, imageFileUri)
    }

    // write image
    with(fileOutputStream) {
        this@save.compress(Bitmap.CompressFormat.JPEG, 100, this)
        this.close()

        Timber.i("Saved crop to ${imageFileUri.path}")
    }

    // trigger refreshing of gallery
    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageFileUri))
}


/**
 * References:
 *      https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
 *      https://developer.android.com/training/data-storage/use-cases#modify-delete-media
 */
private fun Uri.deleteUnderlyingImageFile(context: Context) {
    val file = File(this.imageFilePath(context))

    // delete file and update media gallery
    context.contentResolver.delete(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Images.ImageColumns.DATA + "=?",
        arrayOf(file.canonicalPath)
    )

    // log deletion success if debugging
    if (debuggingModeEnabled()){
        with(file.canonicalFile.absolutePath){
            if (file.exists())
                Timber.e("Deletion of $this failed")
            else
                Timber.e("Successfully deleted $this")
        }
    }
}


private fun Uri.imageFileName(context: Context): String = imageFilePath(context).split('/').last()

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