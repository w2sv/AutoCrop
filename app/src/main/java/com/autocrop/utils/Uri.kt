package com.autocrop.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.File


fun Uri.deleteUnderlyingMediaFile(context: Context)
{
    /* Reference: https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1 */

    val LOG_TAG = "ImageDeletion"

    val file = File(this.imageFilePath(context))

    // delete file and update media gallery
    if (apiLowerEquals(29)){
        context.contentResolver.delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Images.ImageColumns.DATA + "=?",
            arrayOf(file.canonicalPath)
        )
    }
    else
        // https://developer.android.com/training/data-storage/use-cases
        throw NotImplementedError("File deletion for API >= 30 yet to be implemented")

    // log deletion success
    if (file.exists())
        Log.e(LOG_TAG, "couldn't delete ${file.canonicalFile}")
    else
        Log.i(LOG_TAG, "successfully deleted ${file.canonicalFile}")
}


fun Uri.imageFilePath(context: Context): String =
    context.contentResolver.query(
        this, arrayOf(MediaStore.Images.Media.DATA),
        null,
        null,
        null
    ).run {
            this!!.moveToFirst()
            this.getString(this.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))!!
        }


fun Uri.imageFileName(context: Context): String = imageFilePath(context).split('/').last()