package com.autocrop

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import java.io.File


// ------------------
// Uri
// ------------------
fun Uri.getRealPath(context: Context): String? =
    context.contentResolver.query(this, arrayOf(MediaStore.Images.Media.DATA), null, null, null).run {
        this!!.moveToFirst()
        this.getString(this.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
    }


fun Uri.deleteUnderlyingResource(context: Context){
    File(this.getRealPath(context)!!).canonicalFile.delete()
    context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(this.getRealPath(context)!!))))
}


// -------------------
// Image Saving
// -------------------
fun saveCroppedImage(contentResolver: ContentResolver, croppedImage: Bitmap, title: String?){
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
fun displayMessage(text: String, context: Context){
    val toast = Toast.makeText(context, " $text ", Toast.LENGTH_LONG).apply {
        this.view.setBackgroundColor(Color.parseColor("darkgray"))
    }

    (toast.view.findViewById<View>(android.R.id.message) as TextView).apply {
        this.setTextColor(Color.parseColor("white"))
    }

    toast.show()
}


// --------------------
// Layout
// --------------------
fun hideSystemUI(window: Window) {
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            // Set the content to appear under the system bars so that the
            // content doesn't resize when the system bars hide and show.
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Hide the nav bar and status bar
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)
}