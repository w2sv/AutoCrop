package com.example.screenshotboundremoval

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import kotlinx.android.synthetic.main.display_screen.*
import java.io.File


class ProcedureDialog(private val savedImageUri: Uri, private val activityContext: Context) : AppCompatDialogFragment(){

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val images: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, images, null, null, null)

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("How do you want to proceed?")
            .setPositiveButton("Keep Image", KeepButtonOnClickListener())
            .setNegativeButton("Discard", DiscardButtonOnClickListener())

        return builder.create()
    }

    private fun restartMainActivity() = startActivity(Intent(context, MainActivity::class.java))

    // ---------------
    // BUTTON CLASSES
    // ---------------

    private inner class KeepButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) = restartMainActivity()
    }

    private inner class DiscardButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            deleteImage()
            restartMainActivity()
        }

        private fun deleteImage(){
            val deleteFile = File(getRealPathFromURI(activityContext, savedImageUri)!!)

            if (deleteFile.exists()){
                // deleteFile.canonicalFile.delete()
                deleteFile.canonicalFile.deleteRecursively()

                if(deleteFile.exists())
                    activityContext.deleteFile(deleteFile.name)

                activityContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(getRealPathFromURI(activityContext, savedImageUri)!!))))
            }
        }
    }

    /*private class OriginalImageDeletionOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
        }
    }*/
}


class ProcedureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedImageUri: Uri = intent.getParcelableExtra(SAVED_IMAGE_URI)!!

        // reload image
        val reloadedImage: Bitmap? = BitmapFactory.decodeStream(contentResolver.openInputStream(savedImageUri))

        // display cropped image
        setContentView(R.layout.display_screen)  // crashing when procedure activity layout seized
        image_view.setImageBitmap(reloadedImage)

        // query procedure
        openDialog(savedImageUri)
    }
    private fun openDialog(savedImageUri: Uri){
        val dialog = ProcedureDialog(savedImageUri, this)
        dialog.show(supportFragmentManager, "procedure")
    }
}
