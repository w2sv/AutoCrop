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
import java.time.Instant
import java.time.format.DateTimeFormatter


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
    } catch (e: Exception) {
        println( "getRealPathFromURI Exception : $e")
        ""
    } finally {
        cursor?.close()
    }
}

fun Uri.deleteUnderlyingRessource(context: Context){
    val deleteFile = File(this.getRealPath(context)!!)

    if (deleteFile.exists()){
        deleteFile.canonicalFile.delete()

        if(deleteFile.exists())
            context.deleteFile(deleteFile.name)

        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(this.getRealPath(context)!!))))
    }
}

// -------------------
// Images
// -------------------
fun saveCroppedImage(contentResolver: ContentResolver, croppedImage: Bitmap, originalTitle: String?){
    fun alterOriginalTitle(): String = "Cropped" + originalTitle!!.substring(0, originalTitle.indexOf(".")) + ".jpg"
    fun getNewTitle(): String = "CroppedScreenshot" + DateTimeFormatter.ISO_INSTANT.format(
        Instant.now()).toString().replaceMultiple(listOf(":", ".", "_", "T"), "-").removeSuffix("Z") + ".jpg"

    val title: String = if(originalTitle.isNullOrEmpty()) getNewTitle() else alterOriginalTitle()

    MediaStore.Images.Media.insertImage(
        contentResolver,
        croppedImage,
        title,
        "")
}

// -------------------
// Output Message
// -------------------
fun displayMessage(text: String, context: Context){
    val paddedText = " $text "
    val toast = Toast.makeText(context, paddedText, Toast.LENGTH_LONG)
    toast.view.setBackgroundColor(Color.parseColor("darkgray"))

    val view = toast.view.findViewById<View>(android.R.id.message) as TextView
    view.setTextColor(Color.parseColor("white"))

    toast.show()
}