package com.autocrop

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import java.io.File


fun Boolean.toInt(): Int = this.compareTo(false)


// ------------------
// Uri
// ------------------
fun Uri.deleteUnderlyingResource(context: Context){
    /* Reference: https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1 */

    val file = File(this.getRealPath(context)!!)

    // delete file and update media gallery
    if (android.os.Build.VERSION.SDK_INT <= 29){
        context.contentResolver.delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Images.ImageColumns.DATA + "=?" ,
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


fun Uri.getRealPath(context: Context): String? =
    context.contentResolver.query(
        this, arrayOf(MediaStore.Images.Media.DATA),
        null,
        null,
        null)
        .run {
            this!!.moveToFirst()
            this.getString(this.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
        }


// -------------------
// Image Saving
// -------------------
fun saveImage(contentResolver: ContentResolver, croppedImage: Bitmap, title: String?){
    MediaStore.Images.Media.insertImage(
        contentResolver,
        croppedImage,
        title,
        ""
    )
}


// -------------------
// Toast
// -------------------
fun paddedMessage(vararg row: String): String = " ${row.joinToString(" \n ")} "


fun Activity.displayToast(vararg row: String) {
    Toast.makeText(
        this@displayToast,
        paddedMessage(*row),
        Toast.LENGTH_LONG
    ).apply{
        this.view!!.setBackgroundColor(Color.parseColor("darkgray"))
        (this.view.findViewById<View>(android.R.id.message) as TextView).apply {
            this.setTextColor(Color.parseColor("white"))
        }

        this.show()
    }
}


fun Activity.persistMenuAfterItemClick(item: MenuItem): Boolean{
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
    item.setActionView(View(this))
    item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
            return false
        }

        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
            return false
        }
    })

    return false
}