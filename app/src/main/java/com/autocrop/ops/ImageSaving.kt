package com.autocrop.ops

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.autocrop.GlobalParameters
import com.autocrop.utils.android.apiLowerEquals
import com.autocrop.utils.android.deleteUnderlyingImageFile
import com.autocrop.utils.android.imageFileName
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


fun saveImageAndDeleteScreenshotIfApplicable(
    uri: Uri,
    image: Bitmap,
    context: Context
) {
    saveImage(context, image, uri.imageFileName(context))

    // delete screenshot if applicable
    if (GlobalParameters.deleteInputScreenshots)
        uri.deleteUnderlyingImageFile(context)
}


private fun saveImage(context: Context, image: Bitmap, title: String) {
    // https://stackoverflow.com/a/10124040
    // https://stackoverflow.com/a/59536115

    val (fileOutputStream: OutputStream, imageFileUri: Uri) = if (apiLowerEquals(29)) {
        File(
            Environment.getExternalStoragePublicDirectory(GlobalParameters.relativeCropSaveDirPath)
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
                this.put(MediaStore.MediaColumns.RELATIVE_PATH, GlobalParameters.relativeCropSaveDirPath)
                this.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            }
        )!!

        Pair(context.contentResolver.openOutputStream(imageFileUri)!!, imageFileUri)
    }

    // write image
    with(fileOutputStream) {
        image.compress(Bitmap.CompressFormat.JPEG, 100, this)
        this.close()

        Timber.i("Saved image to ${imageFileUri.path}")
    }

    // trigger refreshing of gallery
    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageFileUri))
}