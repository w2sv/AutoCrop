package com.example.screenshotboundremoval

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import java.io.File


// ------------------
// Uri Extensions
// ------------------
fun Uri.getRealPath(context: Context): String?{
    var cursor: Cursor? = null
    return try {
        val images: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        cursor = context.contentResolver.query(this, images, null, null, null)

        val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        cursor.getString(columnIndex)
    } catch (e: Exception) {""}
    finally {
        cursor?.close()
    }
}

fun Uri.deleteUnderlyingResource(context: Context){
    File(this.getRealPath(context)!!).canonicalFile.delete()
    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(this.getRealPath(context)!!))))
}

// -------------------
// Images
// -------------------
fun saveCroppedImage(contentResolver: ContentResolver, croppedImage: Bitmap, originalTitle: String?){
    MediaStore.Images.Media.insertImage(
        contentResolver,
        croppedImage,
        originalTitle,
        "")
}

// -------------------
// Output Message
// -------------------
fun displayMessage(text: String, context: Context){
    val toast = Toast.makeText(context, " $text ", Toast.LENGTH_LONG).apply {
        this.view.setBackgroundColor(Color.parseColor("darkgray"))
    }

    (toast.view.findViewById<View>(android.R.id.message) as TextView).apply {
        this.setTextColor(Color.parseColor("white"))
    }

    toast.show()
}