package com.autocrop.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.File


fun Uri.pathDocument(context: Context): String{
    val DOCUMENT_STR = "/document/"

    fun convertDelimiters(s: String): String = s.replaceMultiple("%3A", "%2F", newValue = "/").replace("%20", " ")

    toString().run {
        val subStr: String = this.slice(this.indexOf(DOCUMENT_STR) + DOCUMENT_STR.length..this.lastIndex)

        subStr.removePrefix("primary").run {
            if (this.length != subStr.length)
                return "/${context.filesDir.absolutePath.substringBefore("Android/data/com.bunsenbrenner.screenshotboundremoval")}/${convertDelimiters(this)}"
        }

        return "/storage/${convertDelimiters(subStr)}"
    }
}

fun Uri.deleteUnderlyingMediaFile(context: Context){
    /* Reference: https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1 */

    val file = File(this.pathMediaFile(context))

    // delete file and update media gallery
    if (android.os.Build.VERSION.SDK_INT <= 29){
        context.contentResolver.delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Images.ImageColumns.DATA + "=?",
            arrayOf(file.canonicalPath)
        )
    }
    else
        // https://developer.android.com/training/data-storage/use-cases
        throw NotImplementedError("File deletion for API > 29 yet to be implemented")

    // log deletion success
    val LOG_TAG = "ImageDeletion"

    if (file.exists())
        Log.e(LOG_TAG, "couldn't delete ${file.canonicalFile}")
    else
        Log.i(LOG_TAG, "successfully deleted ${file.canonicalFile}")
}


fun Uri.pathMediaFile(context: Context): String =
    context.contentResolver.query(
        this, arrayOf(MediaStore.Images.Media.DATA),
        null,
        null,
        null
    ).run {
            this!!.moveToFirst()
            this.getString(this.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))!!
        }


fun Uri.nameMediaFile(context: Context): String = pathMediaFile(context).split('/').last()