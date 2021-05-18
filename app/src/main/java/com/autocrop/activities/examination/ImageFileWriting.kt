package com.autocrop.activities.examination

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import com.autocrop.UserPreferences
import com.autocrop.utils.android.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


fun saveCropAndDeleteScreenshotIfApplicable(
    crop: Bitmap,
    screenshotUri: Uri,
    context: Context) {

    crop.save(context, screenshotUri.cropFileName)

    // delete screenshot if applicable
    if (UserPreferences.deleteInputScreenshots)
        screenshotUri.deleteUnderlyingImageFile(context)
}


/**
 * References:
 *      https://stackoverflow.com/a/10124040
 *      https://stackoverflow.com/a/59536115
 */
private fun Bitmap.save(context: Context, fileName: String) {
    try {
        // set file output stream and target file uri
        val (fileOutputStream: OutputStream, imageFileUri: Uri) = if (apiLowerEquals(29)) {
            File(
                UserPreferences.absoluteCropSaveDirPath,
                fileName
            ).run {
                Pair(FileOutputStream(this), Uri.fromFile(this))
            }

        } else {
            val imageFileUri: Uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        UserPreferences.relativeCropSaveDirPath
                    )
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                }
            )!!

            Pair(context.contentResolver.openOutputStream(imageFileUri)!!, imageFileUri)
        }

        // write image
        with(fileOutputStream) {
            this@save.compress(Bitmap.CompressFormat.JPEG, 100, this)
            close()

            Timber.i("Saved crop to ${imageFileUri.path}")
        }
        imageFileUri.notifyGalleryAboutFileModification(context)

    } catch (e: FileNotFoundException) {
        if (UserPreferences.saveToAutocroppedDir && UserPreferences.makeAutoCroppedDirIfRequired())
            return save(context, fileName)
                .also {
                    Timber.i("Recreated AutoCropped dir")
                }
        else
            throw e
    }
}


private val Uri.cropFileName: String
    get() = fileName
        .replace(
            "screenshot",
            "AutoCrop",
            true
        )


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


private fun Uri.notifyGalleryAboutFileModification(context: Context) {
    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, this))
}


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