package com.autocrop.utils.android

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import timber.log.Timber
import java.io.File


fun Uri.deleteUnderlyingImageFile(context: Context) {
    /* References:
        https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
        https://developer.android.com/training/data-storage/use-cases#modify-delete-media */

    val file = File(this.imageFilePath(context))

    // delete file and update media gallery
    context.contentResolver.delete(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Images.ImageColumns.DATA + "=?",
        arrayOf(file.canonicalPath)
    )

    // log deletion success if debugging
    if (debuggingMode()){
        with(file.canonicalFile.absolutePath){
            if (file.exists())
                Timber.e("Deletion of $this failed")
            else
                Timber.e("Successfully deleted $this")
        }
    }
}


fun Uri.imageFilePath(context: Context): String =
    context.contentResolver.query(
        this,
        arrayOf(MediaStore.Images.Media.DATA),
        null,
        null,
        null
    ).run {
        this!!.moveToFirst()
        this.getString(this.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))!!
    }


fun Uri.imageFileName(context: Context): String = imageFilePath(context).split('/').last()